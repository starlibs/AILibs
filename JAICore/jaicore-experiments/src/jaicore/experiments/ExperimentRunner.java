package jaicore.experiments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;
import jaicore.basic.StringUtil;
import jaicore.basic.sets.SetUtil;

public class ExperimentRunner {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentRunner.class);

	private final IExperimentSetConfig config;
	private final IExperimentSetEvaluator conductor;
	private final SQLAdapter adapter;
	private final Collection<ExperimentDBEntry> knownExperimentEntries = new HashSet<>();

	private final List<String> fieldsForWhichToIgnoreTime;
	private final List<String> fieldsForWhichToIgnoreMemory;

	private static final String FIELD_ID = "experiment_id";
	private static final String FIELD_MEMORY = "memory";
	private static final String FIELD_NUMCPUS = "cpus";
	private static final String FIELD_TIME = "time";

	private Map<String, List<String>> valuesForKeyFields = new HashMap<>();
	private int memoryLimit;
	private int cpuLimit;
	private int totalNumberOfExperiments;

	public ExperimentRunner(final IExperimentSetEvaluator conductor) {

		/* check data base configuration */
		this.config = conductor.getConfig();
		if (this.config.getDBHost() == null) {
			throw new IllegalArgumentException("DB host must not be null in experiment config.");
		}
		if (this.config.getDBUsername() == null) {
			throw new IllegalArgumentException("DB user must not be null in experiment config.");
		}
		if (this.config.getDBPassword() == null) {
			throw new IllegalArgumentException("DB password must not be null in experiment config.");
		}
		if (this.config.getDBDatabaseName() == null) {
			throw new IllegalArgumentException("DB database name must not be null in experiment config.");
		}
		if (this.config.getDBTableName() == null) {
			throw new IllegalArgumentException("DB table must not be null in experiment config.");
		}

		this.fieldsForWhichToIgnoreMemory = (this.config.getFieldsForWhichToIgnoreMemory() != null)
				? this.config.getFieldsForWhichToIgnoreMemory()
				: new ArrayList<>();
		this.fieldsForWhichToIgnoreTime = (this.config.getFieldsForWhichToIgnoreTime() != null)
				? this.config.getFieldsForWhichToIgnoreTime()
				: new ArrayList<>();

		this.conductor = conductor;
		this.adapter = new SQLAdapter(this.config.getDBHost(), this.config.getDBUsername(), this.config.getDBPassword(),
				this.config.getDBDatabaseName(), this.config.getDBSSL());
		this.updateExperimentSetupAccordingToConfig();
	}

	private void updateExperimentSetupAccordingToConfig() {
		this.memoryLimit = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
		if (this.memoryLimit != this.config.getMemoryLimitinMB()) {
			System.err.println("The true memory limit is " + this.memoryLimit + ", which differs from the "
					+ this.config.getMemoryLimitinMB() + " specified in the config! We will write " + this.memoryLimit
					+ " into the database.");
		}
		this.cpuLimit = this.config.getNumberOfCPUs();
		int numExperiments = 1;
		try {
			/* create map of possible values for each key field */
			for (String key : this.config.getKeyFields()) {
				/* this is a hack needed because one cannot retrieve generic configs */
				String propertyVals = this.config.removeProperty(key);
				if (propertyVals == null) {
					throw new IllegalArgumentException("No property values defined for key field \"" + key + "\"");
				}
				List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(s -> s.trim())
						.collect(Collectors.toList());
				this.config.setProperty(key, propertyVals);
				this.valuesForKeyFields.put(key, vals);
				numExperiments *= vals.size();
			}

			this.createTableIfNotExists();
			this.knownExperimentEntries.addAll(this.getConductedExperiments());

		} catch (SQLException e) {
			e.printStackTrace();
			numExperiments = -1;
		}
		this.totalNumberOfExperiments = numExperiments;
	}

	private String getDatabaseFieldnameForConfigEntry(final String configKey) {
		return configKey.substring(0, configKey.length() - 1).replace(".", "_");
	}

	public Collection<ExperimentDBEntry> getConductedExperiments() throws SQLException {
		Collection<ExperimentDBEntry> experimentEntries = new HashSet<>();

		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM ");
		queryStringSB.append(this.config.getDBTableName());
		queryStringSB.append(" WHERE ");
		boolean firstKeyField = true;
		for (String fieldName : this.valuesForKeyFields.keySet()) {
			if (firstKeyField) {
				firstKeyField = false;
			} else {
				queryStringSB.append(" AND ");
			}
			String keyName = this.getDatabaseFieldnameForConfigEntry(fieldName);

			queryStringSB.append(keyName);
			queryStringSB.append(" IN (");

			boolean firstValue = true;
			for (String value : this.valuesForKeyFields.get(fieldName)) {
				if (firstValue) {
					firstValue = false;
				} else {
					queryStringSB.append(",");
				}
				queryStringSB.append("'");
				queryStringSB.append(value);
				queryStringSB.append("'");
			}
			queryStringSB.append(")");
		}

		ResultSet rs = this.adapter.getPreparedStatement(queryStringSB.toString()).executeQuery();
		while (rs.next()) {
			Map<String, String> keyValues = new HashMap<>();
			for (String key : this.config.getKeyFields()) {
				String dbKey = this.getDatabaseFieldnameForConfigEntry(key);
				keyValues.put(dbKey, rs.getString(dbKey));
			}
			experimentEntries.add(new ExperimentDBEntry(rs.getInt(FIELD_ID),
					new Experiment(rs.getInt(FIELD_MEMORY + "_max"), rs.getInt(FIELD_NUMCPUS), keyValues)));
		}
		return experimentEntries;
	}

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(final Map<String, String> values)
			throws FileNotFoundException, IOException, SQLException {
		return this.createAndGetExperimentIfNotConducted(new Experiment(this.memoryLimit, this.cpuLimit, values));
	}

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(final Experiment experiment)
			throws FileNotFoundException, IOException, SQLException {
		/*
		 * first check whether exactly the same experiment (with the same seed) has been
		 * conducted previously
		 */
		Optional<?> existingExperiment = this.knownExperimentEntries.stream()
				.filter(e -> e.getExperiment().equals(experiment)).findAny();
		if (existingExperiment.isPresent()) {
			return null;
		}

		Map<String, Object> valuesToInsert = new HashMap<>(experiment.getValuesOfKeyFields());
		valuesToInsert.put(FIELD_MEMORY + "_max", this.memoryLimit);
		valuesToInsert.put(FIELD_NUMCPUS, this.cpuLimit);

		int id = this.adapter.insert(this.config.getDBTableName(), valuesToInsert);
		return new ExperimentDBEntry(id, experiment);
	}

	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values)
			throws SQLException {
		Collection<String> writableFields = this.config.getResultFields();
		writableFields.add("exception");
		writableFields.add(FIELD_TIME + "_end");
		if (!writableFields.containsAll(values.keySet())) {
			throw new IllegalArgumentException(
					"The value set contains non-result fields: " + SetUtil.difference(values.keySet(), writableFields));
		}
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String memoryUsageInMB = String.valueOf((int) Runtime.getRuntime().totalMemory() / 1024 / 1024);
		Map<String, String> valuesToWrite = new HashMap<>();
		values.keySet().forEach(k -> valuesToWrite.put(k, values.get(k).toString()));
		for (String result : values.keySet()) {
			if (this.config.getResultFields().contains(result)) {
				if (!this.fieldsForWhichToIgnoreTime.contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_TIME, now);
				}
				if (!this.fieldsForWhichToIgnoreMemory.contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_MEMORY, memoryUsageInMB);
				}
			}
		}
		Map<String, String> where = new HashMap<>();
		where.put(FIELD_ID, String.valueOf(exp.getId()));
		this.adapter.update(this.config.getDBTableName(), valuesToWrite, where);
	}

	public Experiment getExperimentForNumber(final int id) {
		if (id < 0) {
			throw new IllegalArgumentException("Experiment ID must be positive!");
		}
		if (id >= this.totalNumberOfExperiments) {
			throw new IllegalArgumentException("Invalid experiment ID " + id + ". Only " + this.totalNumberOfExperiments
					+ " are possible with the given config.");
		}

		/* determine the block sizes for the different iterations */
		Map<String, Integer> blockSizes = new HashMap<>();
		int size = 1;
		List<String> keyOrder = new ArrayList<>(this.config.getKeyFields());
		Collections.reverse(keyOrder);
		for (String key : keyOrder) {
			blockSizes.put(key, size);
			size *= this.valuesForKeyFields.get(key).size();
		}

		/* find the correct experiment */
		Map<String, String> keyFieldValues = new HashMap<>();
		int k = id;
		for (String key : this.config.getKeyFields()) {
			int s = blockSizes.get(key);
			int index = (int) Math.floor(k / s * 1f);
			keyFieldValues.put(this.getDatabaseFieldnameForConfigEntry(key),
					this.valuesForKeyFields.get(key).get(index));
			k = k % s;
		}
		return new Experiment(this.memoryLimit, this.cpuLimit, keyFieldValues);
	}

	public void randomlyConductExperiments(final int maxNumberOfExperiments, final boolean reload) {
		if (this.totalNumberOfExperiments <= 0) {
			System.out.println("Number of total experiments is 0");
			return;
		}

		logger.info("Now conducting new experiment. {}/{} experiments have already been started or even been completed",
				this.knownExperimentEntries.size(), this.totalNumberOfExperiments);

		int numberOfConductedExperiments = 0;
		while (!Thread.interrupted() && this.knownExperimentEntries.size() < this.totalNumberOfExperiments
				&& ((maxNumberOfExperiments > 0) ? numberOfConductedExperiments < maxNumberOfExperiments : true)) {
			if (reload) {
				this.config.reload();
			}
			this.updateExperimentSetupAccordingToConfig();
			int k = (int) Math.floor(Math.random() * this.totalNumberOfExperiments);
			logger.info("Now conducting {}/{}", k, this.totalNumberOfExperiments);
			try {
				Experiment exp = this.getExperimentForNumber(k);
				logger.info("Conduct experiment with key values: {}", exp.getValuesOfKeyFields());
				if (this.conductExperimentIfNotAlreadyConducted(exp)) {
					numberOfConductedExperiments++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Conducts a single experiment
	 *
	 * @param exp
	 * @throws Exception.
	 *             These are not the exceptions thrown by the experiment itself,
	 *             because these are logged into the database. Exceptions thrown
	 *             here are technical exceptions that occur when arranging the
	 *             experiment
	 */
	public boolean conductExperimentIfNotAlreadyConducted(final Experiment exp) throws Exception {

		ExperimentDBEntry expEntry = this.createAndGetExperimentIfNotConducted(exp);
		if (expEntry != null) {
			Map<String, Object> valuesToAddAfterRun = new HashMap<>();

			this.knownExperimentEntries.add(expEntry);
			try {
				System.gc();
				this.conductor.evaluate(expEntry, this.adapter, m -> {
					try {
						this.updateExperiment(expEntry, m);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				});

			} catch (Throwable e) {
				StringBuilder exceptionEntry = new StringBuilder();
				exceptionEntry.append(e.getClass().getName() + "\n" + e.getMessage());
				for (StackTraceElement se : e.getStackTrace()) {
					exceptionEntry.append("\n\t" + se);
				}
				valuesToAddAfterRun.put("exception", exceptionEntry.toString());
				e.printStackTrace();
				System.err.println("Experiment failed due to exception, which has been logged");
			}
			valuesToAddAfterRun.put(FIELD_TIME + "_end",
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			this.updateExperiment(expEntry, valuesToAddAfterRun);
			return true;
		} else {
			return false;
		}
	}

	public void createTableIfNotExists() throws SQLException {
		StringBuilder sql = new StringBuilder();
		StringBuilder keyFields = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS `" + this.config.getDBTableName() + "` (");
		sql.append("`" + FIELD_ID + "` int(10) NOT NULL AUTO_INCREMENT,");
		for (String key : this.config.getKeyFields()) {
			String shortKey = this.getDatabaseFieldnameForConfigEntry(key);
			sql.append("`" + shortKey + "` VARCHAR(100) NOT NULL,");
			keyFields.append("`" + shortKey + "`,");
		}
		sql.append("`" + FIELD_NUMCPUS + "` int(2) NOT NULL,");
		sql.append("`" + FIELD_MEMORY + "_max` int(6) NOT NULL,");
		sql.append("`" + FIELD_TIME + "_start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");

		for (String result : this.config.getResultFields()) {
			sql.append("`" + result + "` VARCHAR(500) NULL,");
			if (!this.fieldsForWhichToIgnoreTime.contains(result)) {
				sql.append("`" + result + "_" + FIELD_TIME + "` TIMESTAMP NULL,");
			}
			if (!this.fieldsForWhichToIgnoreMemory.contains(result)) {
				sql.append("`" + result + "_" + FIELD_MEMORY + "` int(6) NULL,");
			}
		}
		sql.append("`exception` TEXT NULL,");
		sql.append("`" + FIELD_TIME + "_end` TIMESTAMP NULL,");
		sql.append("PRIMARY KEY (`" + FIELD_ID + "`)");
		sql.append(", UNIQUE KEY `keyFields` (" + keyFields.toString() + "`" + FIELD_NUMCPUS + "`, `" + FIELD_MEMORY
				+ "_max`)");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		this.adapter.update(sql.toString(), new String[] {});
	}

	public void randomlyConductExperiments(final boolean reload) {
		this.randomlyConductExperiments(-1, reload);
	}
}
