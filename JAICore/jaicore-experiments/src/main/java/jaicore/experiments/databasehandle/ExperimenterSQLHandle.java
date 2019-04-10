package jaicore.experiments.databasehandle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jaicore.basic.SQLAdapter;
import jaicore.basic.sets.SetUtil;
import jaicore.experiments.Experiment;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.IDatabaseConfig;
import jaicore.experiments.IExperimentDatabaseHandle;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import jaicore.experiments.exceptions.ExperimentUpdateFailedException;

public class ExperimenterSQLHandle implements IExperimentDatabaseHandle {

	private static final String FIELD_ID = "experiment_id";
	private static final String FIELD_MEMORY = "memory";
	private static final String FIELD_HOST = "host";
	private static final String FIELD_NUMCPUS = "cpus";
	private static final String FIELD_TIME = "time";

	private final SQLAdapter adapter;
	private final String tablename;

	/* state variables */
	private final Collection<ExperimentDBEntry> knownExperimentEntries = new HashSet<>();

	private IExperimentSetConfig config;

	private final List<String> fieldsForWhichToIgnoreTime = new ArrayList<>();
	private final List<String> fieldsForWhichToIgnoreMemory = new ArrayList<>();

	public ExperimenterSQLHandle(final SQLAdapter adapter, final String tablename) {
		super();
		this.adapter = adapter;
		this.tablename = tablename;
	}

	public ExperimenterSQLHandle(final IDatabaseConfig config) {
		if (config.getDBHost() == null) {
			throw new IllegalArgumentException("DB host must not be null in experiment config.");
		}
		if (config.getDBUsername() == null) {
			throw new IllegalArgumentException("DB user must not be null in experiment config.");
		}
		if (config.getDBPassword() == null) {
			throw new IllegalArgumentException("DB password must not be null in experiment config.");
		}
		if (config.getDBDatabaseName() == null) {
			throw new IllegalArgumentException("DB database name must not be null in experiment config.");
		}
		if (config.getDBTableName() == null) {
			throw new IllegalArgumentException("DB table must not be null in experiment config.");
		}
		adapter = new SQLAdapter(config.getDBHost(), config.getDBUsername(), config.getDBPassword(), config.getDBDatabaseName(), config.getDBSSL());
		tablename = config.getDBTableName();
	}

	@Override
	public void setup(final IExperimentSetConfig config) throws ExperimentDBInteractionFailedException {
		this.config = config;

		/* set fields for which to ignore memory and time */
		if (this.config.getFieldsForWhichToIgnoreMemory() != null) {
			fieldsForWhichToIgnoreMemory.addAll(this.config.getFieldsForWhichToIgnoreMemory());
		}
		if (this.config.getFieldsForWhichToIgnoreTime() != null) {
			fieldsForWhichToIgnoreTime.addAll(this.config.getFieldsForWhichToIgnoreTime());
		}

		/* creates a new table if not existent already */
		StringBuilder sql = new StringBuilder();
		StringBuilder keyFields = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS `" + tablename + "` (");
		sql.append("`" + FIELD_ID + "` int(10) NOT NULL AUTO_INCREMENT,");
		for (String key : this.config.getKeyFields()) {
			String shortKey = getDatabaseFieldnameForConfigEntry(key);
			sql.append("`" + shortKey + "` VARCHAR(100) NOT NULL,");
			keyFields.append("`" + shortKey + "`,");
		}
		sql.append("`" + FIELD_NUMCPUS + "` int(2) NOT NULL,");
		sql.append("`" + FIELD_HOST + "` varchar(255) NOT NULL,");
		sql.append("`" + FIELD_MEMORY + "_max` int(6) NOT NULL,");
		sql.append("`" + FIELD_TIME + "_start` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");

		for (String result : this.config.getResultFields()) {
			sql.append("`" + result + "` VARCHAR(500) NULL,");
			if (!fieldsForWhichToIgnoreTime.contains(result)) {
				sql.append("`" + result + "_" + FIELD_TIME + "` TIMESTAMP NULL,");
			}
			if (!fieldsForWhichToIgnoreMemory.contains(result)) {
				sql.append("`" + result + "_" + FIELD_MEMORY + "` int(6) NULL,");
			}
		}
		sql.append("`exception` TEXT NULL,");
		sql.append("`" + FIELD_TIME + "_end` TIMESTAMP NULL,");
		sql.append("PRIMARY KEY (`" + FIELD_ID + "`)");
		sql.append(", UNIQUE KEY `keyFields` (" + keyFields.toString() + "`" + FIELD_NUMCPUS + "`, `" + FIELD_MEMORY + "_max`)");
		sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		try {
			adapter.update(sql.toString(), new String[] {});
		} catch (SQLException e) {
			System.err.println(sql.toString());
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public Collection<ExperimentDBEntry> getConductedExperiments() throws ExperimentDBInteractionFailedException {
		Collection<ExperimentDBEntry> experimentEntries = new HashSet<>();

		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM ");
		queryStringSB.append(tablename);
		//		queryStringSB.append(" WHERE ");
		//		boolean firstKeyField = true;
		//		for (String fieldName : this.valuesForKeyFields.keySet()) {
		//			if (firstKeyField) {
		//				firstKeyField = false;
		//			} else {
		//				queryStringSB.append(" AND ");
		//			}
		//			String keyName = this.getDatabaseFieldnameForConfigEntry(fieldName);
		//
		//			queryStringSB.append(keyName);
		//			queryStringSB.append(" IN (");
		//
		//			boolean firstValue = true;
		//			for (String value : this.valuesForKeyFields.get(fieldName)) {
		//				if (firstValue) {
		//					firstValue = false;
		//				} else {
		//					queryStringSB.append(",");
		//				}
		//				queryStringSB.append("'");
		//				queryStringSB.append(value);
		//				queryStringSB.append("'");
		//			}
		//			queryStringSB.append(")");
		//		}

		try (ResultSet rs = adapter.getPreparedStatement(queryStringSB.toString()).executeQuery()) {
			while (rs.next()) {
				Map<String, String> keyValues = new HashMap<>();
				for (String key : config.getKeyFields()) {
					String dbKey = getDatabaseFieldnameForConfigEntry(key);
					keyValues.put(dbKey, rs.getString(dbKey));
				}
				experimentEntries.add(new ExperimentDBEntry(rs.getInt(FIELD_ID), new Experiment(rs.getInt(FIELD_MEMORY + "_max"), rs.getInt(FIELD_NUMCPUS), keyValues)));
			}
			knownExperimentEntries.addAll(experimentEntries);
			return experimentEntries;
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	public ExperimentDBEntry createAndGetExperimentIfNotConducted(final Map<String, String> values) throws ExperimentDBInteractionFailedException{
		return this.createAndGetExperimentIfNotConducted(new Experiment(config.getMemoryLimitInMB(), config.getNumberOfCPUs(), values));
	}

	@Override
	public ExperimentDBEntry createAndGetExperimentIfNotConducted(final Experiment experiment) throws ExperimentDBInteractionFailedException {

		try {
			/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
			Optional<?> existingExperiment = knownExperimentEntries.stream().filter(e -> e.getExperiment().equals(experiment)).findAny();
			if (existingExperiment.isPresent()) {
				return null;
			}

			Map<String, Object> valuesToInsert = new HashMap<>(experiment.getValuesOfKeyFields());
			valuesToInsert.put(FIELD_MEMORY + "_max", experiment.getMemoryInMB());
			valuesToInsert.put(FIELD_NUMCPUS, experiment.getNumCPUs());
			valuesToInsert.put(FIELD_HOST, InetAddress.getLocalHost().getHostName());
			int id = adapter.insert(tablename, valuesToInsert);
			return new ExperimentDBEntry(id, experiment);
		} catch (UnknownHostException | SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException {
		Collection<String> writableFields = config.getResultFields();
		writableFields.add("exception");
		writableFields.add(FIELD_TIME + "_end");
		if (!writableFields.containsAll(values.keySet())) {
			throw new IllegalArgumentException("The value set contains non-result fields: " + SetUtil.difference(values.keySet(), writableFields));
		}
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String memoryUsageInMB = String.valueOf((int) Runtime.getRuntime().totalMemory() / 1024 / 1024);
		Map<String, String> valuesToWrite = new HashMap<>();
		values.keySet().forEach(k -> valuesToWrite.put(k, values.get(k).toString()));
		for (String result : values.keySet()) {
			if (config.getResultFields().contains(result)) {
				if (!fieldsForWhichToIgnoreTime.contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_TIME, now);
				}
				if (!fieldsForWhichToIgnoreMemory.contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_MEMORY, memoryUsageInMB);
				}
			}
		}
		Map<String, String> where = new HashMap<>();
		where.put(FIELD_ID, String.valueOf(exp.getId()));
		try {
			adapter.update(tablename, valuesToWrite, where);
		}
		catch (SQLException e) {
			throw new ExperimentUpdateFailedException(e);
		}
	}

	@Override
	public void finishExperiment(final ExperimentDBEntry expEntry, final Throwable error) throws ExperimentDBInteractionFailedException {
		Map<String, Object> valuesToAddAfterRun = new HashMap<>();
		if (error != null) {
			StringBuilder exceptionEntry = new StringBuilder();
			exceptionEntry.append(error.getClass().getName() + "\n" + error.getMessage());
			for (StackTraceElement se : error.getStackTrace()) {
				exceptionEntry.append("\n\t" + se);
			}
			valuesToAddAfterRun.put("exception", exceptionEntry.toString());
		}
		valuesToAddAfterRun.put(FIELD_TIME + "_end", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		updateExperiment(expEntry, valuesToAddAfterRun);
	}

	@Override
	public void finishExperiment(final ExperimentDBEntry expEntry) throws ExperimentDBInteractionFailedException {
		this.finishExperiment(expEntry, null);
	}

	private String getDatabaseFieldnameForConfigEntry(final String configKey) {
		return configKey.replace("\\.", "_");
	}
}
