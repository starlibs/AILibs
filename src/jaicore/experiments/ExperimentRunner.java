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

import jaicore.basic.MySQLAdapter;
import jaicore.basic.SetUtil;
import jaicore.basic.StringUtil;

public class ExperimentRunner {

	private final IExperimentSetConfig config;
	private final ISingleExperimentConductor conductor;
	private final MySQLAdapter adapter;
	private final Collection<ExperimentDBEntry> knownExperimentEntries = new HashSet<>();
	
	private static final String FIELD_ID = "experiment_id";
	private static final String FIELD_MEMORY = "memory";
	private static final String FIELD_NUMCPUS = "cpus";
	private static final String FIELD_TIME = "time";

	private Map<String, List<String>> valuesForKeyFields = new HashMap<>();
	private int memoryLimit;
	private int cpuLimit;
	private int totalNumberOfExperiments;

	public ExperimentRunner(IExperimentSetConfig config, ISingleExperimentConductor conductor) {
		
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

		this.conductor = conductor;
		this.config = config;
		this.adapter = new MySQLAdapter(config.getDBHost(), config.getDBUsername(), config.getDBPassword(), config.getDBDatabaseName());
		updateExperimentSetupAccordingToConfig();
	}
	
	private void updateExperimentSetupAccordingToConfig() {
		this.memoryLimit = (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);
		if (memoryLimit != config.getMemoryLimitinMB()) {
			System.err.println("The true memory limit is " + memoryLimit + ", which differs from the " + config.getMemoryLimitinMB() + " specified in the config! We will write "
					+ memoryLimit + " into the database.");
		}
		this.cpuLimit = config.getNumberOfCPUs();
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
			experimentEntries.add(new ExperimentDBEntry(rs.getInt(FIELD_ID), new Experiment(rs.getInt(FIELD_MEMORY + "_max"), rs.getInt(FIELD_NUMCPUS), keyValues)));
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
		valuesToInsert.put(FIELD_MEMORY + "_max", memoryLimit);
		valuesToInsert.put(FIELD_NUMCPUS, cpuLimit);

		int id = adapter.insert(config.getDBTableName(), valuesToInsert);
		return new ExperimentDBEntry(id, experiment);
	}

	public void updateExperiment(ExperimentDBEntry exp, Map<String, ? extends Object> values) throws SQLException {
		Collection<String> writableFields = config.getResultFields();
		writableFields.add("exception");
		writableFields.add(FIELD_TIME + "_end");
		if (!writableFields.containsAll(values.keySet()))
			throw new IllegalArgumentException("The value set contains non-result fields: " + SetUtil.difference(values.keySet(), writableFields));
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String memoryUsageInMB = String.valueOf((int)Runtime.getRuntime().totalMemory() / 1024 / 1024);
		Map<String,String> valuesToWrite = new HashMap<>();
		values.keySet().forEach(k -> valuesToWrite.put(k, values.get(k).toString()));
		for (String result : values.keySet()) {
			if (config.getResultFields().contains(result)) {
				valuesToWrite.put(result + "_" + FIELD_TIME, now);
				valuesToWrite.put(result + "_" + FIELD_MEMORY, memoryUsageInMB);
			}
		}
		Map<String, String> where = new HashMap<>();
		where.put(FIELD_ID, String.valueOf(exp.getId()));
		adapter.update(config.getDBTableName(), valuesToWrite, where);
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
			config.reload();
			updateExperimentSetupAccordingToConfig();
			int k = (int) Math.floor(Math.random() * totalNumberOfExperiments);
			System.out.println("Now conducting " + k +"/" + totalNumberOfExperiments);
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
			Map<String, Object> valuesToAddAfterRun = new HashMap<>();
			
			knownExperimentEntries.add(expEntry);
			try {
				System.gc();
				conductor.conduct(expEntry, adapter, m -> {
					try {
						updateExperiment(expEntry, m);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				});
				
			} catch (Throwable e) {
				valuesToAddAfterRun.put("exception", e.getClass().getName() + "\n" + e.getMessage());
				System.err.println("Experiment failed due to exception, which has been logged");
			}
			valuesToAddAfterRun.put(FIELD_TIME + "_end", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			updateExperiment(expEntry, valuesToAddAfterRun);
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
		sql.append("`" + FIELD_MEMORY + "_max` int(6) NOT NULL,");
		sql.append("`" + FIELD_TIME + "_start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");

		for (String result : config.getResultFields()) {
			sql.append("`" + result + "` VARCHAR(100) NULL,");
			sql.append("`" + result + "_" + FIELD_TIME + "` TIMESTAMP NULL,");
			sql.append("`" + result + "_" + FIELD_MEMORY + "` int(6) NULL,");
		}
		sql.append("`exception` TEXT NULL,");
		sql.append("`" + FIELD_TIME + "_end` TIMESTAMP NULL,");
		sql.append("PRIMARY KEY (`" + FIELD_ID + "`)");
		sql.append(", UNIQUE KEY `keyFields` (" + keyFields.toString() + "`" + FIELD_NUMCPUS + "`, `" + FIELD_MEMORY + "_max`)");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		adapter.update(sql.toString(), new String[] {});
	}
}
