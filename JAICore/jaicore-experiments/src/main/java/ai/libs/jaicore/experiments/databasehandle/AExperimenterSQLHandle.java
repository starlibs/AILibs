package ai.libs.jaicore.experiments.databasehandle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.PreparedStatement;

import ai.libs.jaicore.basic.IDatabaseConfig;
import ai.libs.jaicore.basic.SQLAdapter;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.IExperimentDatabaseHandle;
import ai.libs.jaicore.experiments.IExperimentSetConfig;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentUpdateFailedException;

public class AExperimenterSQLHandle implements IExperimentDatabaseHandle {

	private static final Logger logger = LoggerFactory.getLogger(AExperimenterSQLHandle.class);

	private static final String FIELD_ID = "experiment_id";
	private static final String FIELD_MEMORY = "memory";
	private static final String FIELD_HOST = "host";
	private static final String FIELD_NUMCPUS = "cpus";
	private static final String FIELD_TIME = "time";

	private final SQLAdapter adapter;
	private final String tablename;

	private IExperimentSetConfig config;

	public AExperimenterSQLHandle(final SQLAdapter adapter, final String tablename) {
		super();
		this.adapter = adapter;
		this.tablename = tablename;
	}

	public AExperimenterSQLHandle(final IDatabaseConfig config) {
		if (config.getDBDriver() == null) {
			throw new IllegalArgumentException("DB driver must not be null in experiment config.");
		}
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
		this.adapter = new SQLAdapter(config.getDBDriver(), config.getDBHost(), config.getDBUsername(), config.getDBPassword(), config.getDBDatabaseName(), null, config.getDBSSL() == null || config.getDBSSL());
		this.tablename = config.getDBTableName();
	}

	@Override
	public void setup(final IExperimentSetConfig config) throws ExperimentDBInteractionFailedException {
		this.config = config;

		/* creates basic table creation statement */
		StringBuilder sqlMainTable = new StringBuilder();
		StringBuilder keyFields = new StringBuilder();
		sqlMainTable.append("CREATE TABLE IF NOT EXISTS `" + this.tablename + "` (");
		sqlMainTable.append("`" + FIELD_ID + "` int(10) NOT NULL AUTO_INCREMENT,");
		for (String key : this.config.getKeyFields()) {
			String shortKey = this.getDatabaseFieldnameForConfigEntry(key);
			sqlMainTable.append("`" + shortKey + "` VARCHAR(1000) NOT NULL,");
			keyFields.append("`" + shortKey + "`,");
		}
		sqlMainTable.append("`" + FIELD_NUMCPUS + "` int(2) NOT NULL,");
		sqlMainTable.append("`" + FIELD_MEMORY + "_max` int(6) NOT NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
		sqlMainTable.append("`" + FIELD_HOST + "` varchar(255) NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_started` TIMESTAMP NULL,");

		/* add columns for result fields */
		for (String result : this.config.getResultFields()) {
			sqlMainTable.append("`" + result + "` VARCHAR(500) NULL,");
			if (this.config.getFieldsForWhichToIgnoreTime() == null || !this.config.getFieldsForWhichToIgnoreTime().contains(result)) {
				sqlMainTable.append("`" + result + "_" + FIELD_TIME + "` TIMESTAMP NULL,");
			}
			if (config.getFieldsForWhichToIgnoreMemory() == null || !config.getFieldsForWhichToIgnoreMemory().contains(result)) {
				sqlMainTable.append("`" + result + "_" + FIELD_MEMORY + "` int(6) NULL,");
			}
		}

		/* exception field and keys */
		sqlMainTable.append("`exception` TEXT NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_end` TIMESTAMP NULL,");
		sqlMainTable.append("PRIMARY KEY (`" + FIELD_ID + "`)");
		//		sqlMainTable.append(", INDEX `keyFields` (" + keyFields.toString() + "`" + FIELD_NUMCPUS + "`, `" + FIELD_MEMORY + "_max`)");
		sqlMainTable.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		try {
			this.adapter.update(sqlMainTable.toString(), new String[] {});
		} catch (SQLException e) {
			logger.error("An SQL exception occured with the following query: {}", sqlMainTable);
			throw new ExperimentDBInteractionFailedException(e);
		}
	}



	@Override
	public List<ExperimentDBEntry> getAllExperiments() throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.config.getKeyFields() == null) {
			throw new IllegalStateException("No key fields defined. Setup the handler before using it.");
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM ");
		queryStringSB.append(this.tablename);
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getOpenExperiments() throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.config.getKeyFields() == null) {
			throw new IllegalStateException("No key fields defined. Setup the handler before using it.");
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM `");
		queryStringSB.append(this.tablename);
		queryStringSB.append("` WHERE time_started IS NULL");
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getRunningExperiments() throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.config.getKeyFields() == null) {
			throw new IllegalStateException("No key fields defined. Setup the handler before using it.");
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM `");
		queryStringSB.append(this.tablename);
		queryStringSB.append("` WHERE time_started IS NOT NULL AND time_end IS NULL");
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getConductedExperiments() throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.config.getKeyFields() == null) {
			throw new IllegalStateException("No key fields defined. Setup the handler before using it.");
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM `");
		queryStringSB.append(this.tablename);
		queryStringSB.append("` WHERE time_started IS NOT NULL");
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	private List<ExperimentDBEntry> getExperimentsForSQLQuery(final String sql) throws SQLException {
		try (ResultSet rs = this.adapter.getPreparedStatement(sql).executeQuery()) {
			return this.getAllFromResultSet(rs);
		}
	}

	private List<ExperimentDBEntry> getAllFromResultSet(final ResultSet rs) throws SQLException {
		List<ExperimentDBEntry> experimentEntries = new ArrayList<>(rs.getFetchSize());
		while (rs.next()) {
			experimentEntries.add(this.getCurrentFromResultSet(rs));
		}
		return experimentEntries;
	}

	private ExperimentDBEntry getCurrentFromResultSet(final ResultSet rs) throws SQLException {

		/* get key values for experiment */
		Map<String, String> keyValues = new HashMap<>();
		for (String key : this.config.getKeyFields()) {
			String dbKey = this.getDatabaseFieldnameForConfigEntry(key);
			keyValues.put(dbKey, rs.getString(dbKey));
		}

		/* get result values for experiment */
		Map<String, Object> resultValues = new HashMap<>();
		for (String key : this.config.getResultFields()) {
			String dbKey = this.getDatabaseFieldnameForConfigEntry(key);
			resultValues.put(dbKey, rs.getString(dbKey));
		}

		return new ExperimentDBEntry(rs.getInt(FIELD_ID), new Experiment(rs.getInt(FIELD_MEMORY + "_max"), rs.getInt(FIELD_NUMCPUS), keyValues, resultValues));
	}

	public ExperimentDBEntry createAndGetExperiment(final Map<String, String> values) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {
		return this.createAndGetExperiment(new Experiment(this.config.getMemoryLimitInMB(), this.config.getNumberOfCPUs(), values));
	}

	@Override
	public ExperimentDBEntry createAndGetExperiment(final Experiment experiment) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {
		try {

			/* first check whether exactly the same experiment (with the same seed) has been conducted previously */
			Optional<?> existingExperiment = this.getConductedExperiments().stream().filter(e -> e.getExperiment().equals(experiment)).findAny();
			if (existingExperiment.isPresent()) {
				throw new ExperimentAlreadyExistsInDatabaseException();
			}

			Map<String, Object> valuesToInsert = new HashMap<>(experiment.getValuesOfKeyFields());
			valuesToInsert.put(FIELD_MEMORY + "_max", experiment.getMemoryInMB());
			valuesToInsert.put(FIELD_NUMCPUS, experiment.getNumCPUs());
			logger.debug("Inserting mem: {}, cpus: {}, host: {}, and key fields: {}", experiment.getMemoryInMB(), experiment.getNumCPUs(), valuesToInsert.get(FIELD_HOST), experiment.getValuesOfKeyFields());
			int id = this.adapter.insert(this.tablename, valuesToInsert);
			return new ExperimentDBEntry(id, experiment);
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> createAndGetExperiments(final List<Experiment> experiments) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {

		/* derive input for insertion */
		List<String> keys = new ArrayList<>();
		keys.add(FIELD_MEMORY + "_max");
		keys.add(FIELD_NUMCPUS);
		keys.addAll(this.config.getKeyFields());

		this.config.getKeyFields();
		List<List<?>> values = new ArrayList<>();
		for (Experiment exp : experiments) {
			List<String> datarow = new ArrayList<>(keys.size());
			datarow.add("" + exp.getMemoryInMB());
			datarow.add("" + exp.getNumCPUs());
			for (String key : this.config.getKeyFields()) {
				datarow.add(exp.getValuesOfKeyFields().get(key));
			}
			values.add(datarow);
		}

		/* conduct insertion */
		try {
			List<Integer> ids = this.adapter.insertMultiple(this.tablename, keys, values);
			int n = ids.size();
			List<ExperimentDBEntry> entries = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				entries.add(new ExperimentDBEntry(ids.get(i), experiments.get(i)));
			}
			return entries;
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}


	@Override
	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException {
		Collection<String> writableFields = this.config.getResultFields();
		writableFields.add(FIELD_HOST);
		writableFields.add(FIELD_TIME + "_started");
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
			if (this.config.getResultFields().contains(result)) {
				if (this.config.getFieldsForWhichToIgnoreTime() == null || !this.config.getFieldsForWhichToIgnoreTime().contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_TIME, now);
				}
				if (this.config.getFieldsForWhichToIgnoreMemory() == null || !this.config.getFieldsForWhichToIgnoreMemory().contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_MEMORY, memoryUsageInMB);
				}
			}
		}
		Map<String, String> where = new HashMap<>();
		where.put(FIELD_ID, String.valueOf(exp.getId()));
		try {
			this.adapter.update(this.tablename, valuesToWrite, where);
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
		this.updateExperiment(expEntry, valuesToAddAfterRun);
	}

	@Override
	public void finishExperiment(final ExperimentDBEntry expEntry) throws ExperimentDBInteractionFailedException {
		this.finishExperiment(expEntry, null);
	}

	private String getDatabaseFieldnameForConfigEntry(final String configKey) {
		return configKey.replace("\\.", "_");
	}

	@Override
	public void deleteExperiment(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException {
		try (PreparedStatement statement = (PreparedStatement) this.adapter.getPreparedStatement("DELETE FROM `" + this.tablename + "` WHERE experiment_id = ?")) {
			statement.setInt(1, exp.getId());
			statement.execute();
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}


	@Override
	public void deleteDatabase() throws ExperimentDBInteractionFailedException {
		try (PreparedStatement statement = (PreparedStatement) this.adapter.getPreparedStatement("DROP TABLE `" + this.tablename + "`")) {
			statement.execute();
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public void startExperiment(final ExperimentDBEntry exp) throws ExperimentUpdateFailedException {
		Map<String, Object> initValues = new HashMap<>();
		initValues.put(FIELD_TIME + "_started", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		try {
			initValues.put(FIELD_HOST, InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			throw new ExperimentUpdateFailedException(e);
		}
		this.updateExperiment(exp, initValues);
	}
}
