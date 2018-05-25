package jaicore.experiments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jaicore.basic.MySQLAdapter;
import jaicore.basic.SetUtil;
import jaicore.basic.StringUtil;

public class ExperimentRunner {

	private final IExperimentConfig config;
	private final ISingleExperimentConductor conductor;
	private final MySQLAdapter adapter;
	private final Collection<ExperimentDBEntry> knownExperimentEntries = new HashSet<>();
	private final int totalNumberOfExperiments;

	private static final String FIELD_ID = "experiment_id";
	private static final String FIELD_MEMORY = "memory";
	private static final String FIELD_NUMCPUS = "cpus";

	private final Map<String, List<String>> valuesForKeyFields = new HashMap<>();
	private final int memoryLimit;
	private final int cpuLimit;

	public ExperimentRunner(IExperimentConfig config, ISingleExperimentConductor conductor) {

		/* check data base configuration */
		if (config.getDBHost() == null)
			throw new IllegalArgumentException("DB host must not be null in experiment config.");
		if (config.getDBUsername() == null)
			throw new IllegalArgumentException("DB user must not be null in experiment config.");
		if (config.getDBPassword() == null)
			throw new IllegalArgumentException("DB password must not be null in experiment config.");
		if (config.getDBDatabaseName() == null)
			throw new IllegalArgumentException("DB database name must not be null in experiment config.");
		if (config.getDBTableName() == null)
			throw new IllegalArgumentException("DB table must not be null in experiment config.");

		/* check memory and cpu constraints */
		this.config = config;
		this.conductor = conductor;
		this.memoryLimit = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
		if (memoryLimit != config.getMemoryLimitinMB()) {
			System.err.println("The true memory limit is " + memoryLimit + ", which differs from the " + config.getMemoryLimitinMB() + " specified in the config! We will write "
					+ memoryLimit + " into the database.");
		}
		this.cpuLimit = config.getNumberOfCPUs();
		this.adapter = new MySQLAdapter(config.getDBHost(), config.getDBUsername(), config.getDBPassword(), config.getDBDatabaseName());
		int numExperiments = 1;
		try {
			createTableIfNotExists();
			knownExperimentEntries.addAll(getConductedExperiments());

			/* create map of possible values for each key field */
			for (String key : config.getKeyFields()) {

				/* this is a hack needed because one cannot retrieve generic configs */
				String propertyVals = config.removeProperty(key);
				List<String> vals = Arrays.asList(StringUtil.explode(propertyVals, ",")).stream().map(s -> s.trim()).collect(Collectors.toList());
				config.setProperty(key, propertyVals);
				valuesForKeyFields.put(key, vals);
				numExperiments *= vals.size();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			numExperiments = -1;
		}
		totalNumberOfExperiments = numExperiments;
	}

	private String getDatabaseFieldnameForConfigEntry(String configKey) {
		return configKey.substring(0, configKey.length() - 1);
	}

	public Collection<ExperimentDBEntry> getConductedExperiments() throws SQLException {
		Collection<ExperimentDBEntry> experimentEntries = new HashSet<>();
		ResultSet rs = adapter.getRowsOfTable(config.getDBTableName());
		while (rs.next()) {
			Map<String, String> keyValues = new HashMap<>();
			for (String key : config.getKeyFields()) {
				String dbKey = getDatabaseFieldnameForConfigEntry(key);
				keyValues.put(dbKey, rs.getString(dbKey));
			}
			experimentEntries.add(new ExperimentDBEntry(rs.getInt(FIELD_ID), new Experiment(rs.getInt(FIELD_MEMORY), rs.getInt(FIELD_NUMCPUS), keyValues)));
		}
		return experimentEntries;
	}

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(Map<String, String> values) throws FileNotFoundException, IOException, SQLException {
		return createAndGetExperimentIfNotConducted(new Experiment(memoryLimit, cpuLimit, values));
	}

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(Experiment experiment) throws FileNotFoundException, IOException, SQLException {

		/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
		Optional<?> existingExperiment = knownExperimentEntries.stream().filter(e -> e.getExperiment().equals(experiment)).findAny();
		if (existingExperiment.isPresent())
			return null;

		Map<String, Object> valuesToInsert = new HashMap<>(experiment.getValuesOfKeyFields());
		valuesToInsert.put(FIELD_MEMORY, memoryLimit);
		valuesToInsert.put(FIELD_NUMCPUS, cpuLimit);

		int id = adapter.insert(config.getDBTableName(), valuesToInsert);
		return new ExperimentDBEntry(id, experiment);
	}

	public void updateExperiment(ExperimentDBEntry exp, Map<String, ? extends Object> values) throws SQLException {
		Map<String, String> where = new HashMap<>();
		where.put(FIELD_ID, String.valueOf(exp.getId()));
		adapter.update(config.getDBTableName(), values, where);
	}

	public Experiment getExperimentForNumber(int id) {

		if (id < 0)
			throw new IllegalArgumentException("Experiment ID must be positive!");
		if (id >= totalNumberOfExperiments)
			throw new IllegalArgumentException("Invalid experiment ID " + id + ". Only " + totalNumberOfExperiments + " are possible with the given config.");

		/* determine the block sizes for the different iterations */
		Map<String, Integer> blockSizes = new HashMap<>();
		int size = 1;
		List<String> keyOrder = new ArrayList<>(config.getKeyFields());
		Collections.reverse(keyOrder);
		for (String key : keyOrder) {
			blockSizes.put(key, size);
			size *= valuesForKeyFields.get(key).size();
		}

		/* find the correct experiment */
		Map<String, String> keyFieldValues = new HashMap<>();
		int k = id;
		for (String key : config.getKeyFields()) {
			int s = blockSizes.get(key);
			int index = (int) Math.floor(k / s * 1f);
			keyFieldValues.put(getDatabaseFieldnameForConfigEntry(key), valuesForKeyFields.get(key).get(index));
			k = k % s;
		}
		return new Experiment(memoryLimit, cpuLimit, keyFieldValues);
	}

	public void randomlyConductExperiments() {
		if (totalNumberOfExperiments <= 0) {
			System.out.println("Number of total experiments is 0");
			return;
		}
		while (!Thread.interrupted() && knownExperimentEntries.size() < totalNumberOfExperiments) {
			int k = (int) Math.floor(Math.random() * totalNumberOfExperiments);
			try {
				Experiment exp = getExperimentForNumber(k);
				conductExperimentIfNotAlreadyConducted(exp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Conducts a single experiment
	 * 
	 * @param exp
	 * @throws Exception. These are not the exceptions thrown by the experiment itself, because these are logged into the database. Exceptions thrown here are technical exceptions that occur when arranging the experiment
	 */
	public void conductExperimentIfNotAlreadyConducted(Experiment exp) throws Exception {

		ExperimentDBEntry expEntry = createAndGetExperimentIfNotConducted(exp);
		if (expEntry != null) {
			Map<String, Object> where = new HashMap<>();
			where.put(FIELD_ID, expEntry.getId());
			knownExperimentEntries.add(expEntry);
			Map<String, Object> results = null;
			try {
				results = conductor.conduct(expEntry, adapter);
			} catch (Throwable e) {
				Map<String, Object> values = new HashMap<>();
				values.put("exception", e.getClass().getName() + "\n" + e.getMessage());
				adapter.update(config.getDBTableName(), values, where);
				System.err.println("Experiment failed due to exception, which has been logged");
				return;
			}
			if (!config.getResultFields().containsAll(results.keySet()))
				throw new IllegalArgumentException("The result set contains non-result fields: " + SetUtil.difference(results.keySet(), config.getResultFields()));
			adapter.update(config.getDBTableName(), results, where);
		}
	}

	public void createTableIfNotExists() throws SQLException {
		StringBuilder sql = new StringBuilder();
		StringBuilder keyFields = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS `" + config.getDBTableName() + "` (");
		sql.append("`" + FIELD_ID + "` int(10) NOT NULL AUTO_INCREMENT,");
		for (String key : config.getKeyFields()) {
			String shortKey = getDatabaseFieldnameForConfigEntry(key);
			sql.append("`" + shortKey + "` VARCHAR(100) NOT NULL,");
			keyFields.append("`" + shortKey + "`,");
		}
		sql.append("`" + FIELD_NUMCPUS + "` int(2) NOT NULL,");
		sql.append("`" + FIELD_MEMORY + "` int(6) NOT NULL,");
		for (String key : config.getResultFields()) {
			sql.append("`" + key + "` VARCHAR(100) NULL,");
		}
		sql.append("`exception` TEXT NULL,");
		sql.append("PRIMARY KEY (`" + FIELD_ID + "`)");
		sql.append(", UNIQUE KEY `keyFields` (" + keyFields.toString() + "`" + FIELD_NUMCPUS + "`, `" + FIELD_MEMORY + "`)");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		adapter.update(sql.toString(), new String[] {});
	}
}
