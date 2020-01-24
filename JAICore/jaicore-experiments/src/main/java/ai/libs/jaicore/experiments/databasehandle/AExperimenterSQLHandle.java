package ai.libs.jaicore.experiments.databasehandle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.db.sql.SQLAdapter;
import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.IExperimentDatabaseHandle;
import ai.libs.jaicore.experiments.IExperimentSetConfig;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyExistsInDatabaseException;
import ai.libs.jaicore.experiments.exceptions.ExperimentAlreadyStartedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentUpdateFailedException;
import ai.libs.jaicore.logging.LoggerUtil;

public class AExperimenterSQLHandle implements IExperimentDatabaseHandle, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(AExperimenterSQLHandle.class);

	private static final String ERROR_NOSETUP = "No key fields defined. Setup the handler before using it.";
	private static final String FIELD_ID = "experiment_id";
	private static final String FIELD_MEMORY = "memory";
	private static final String FIELD_MEMORY_MAX = FIELD_MEMORY + "_max";
	private static final String FIELD_HOST = "host";
	private static final String FIELD_NUMCPUS = "cpus";
	private static final String FIELD_TIME = "time";
	private static final String FIELD_TIME_START = FIELD_TIME + "_started";
	private static final String FIELD_TIME_END = FIELD_TIME + "_end";
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	protected final IDatabaseAdapter adapter;
	protected final String tablename;

	private IExperimentSetConfig config;

	private String[] keyFields;
	private String[] resultFields;

	public AExperimenterSQLHandle(final IDatabaseAdapter adapter, final String tablename) {
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

	protected String getSetupCreateTableQuery() {
		StringBuilder sqlMainTable = new StringBuilder();
		StringBuilder keyFieldsSB = new StringBuilder();
		sqlMainTable.append("CREATE TABLE IF NOT EXISTS `" + this.tablename + "` (");
		sqlMainTable.append("`" + FIELD_ID + "` int(10) NOT NULL AUTO_INCREMENT,");
		for (String key : this.keyFields) {
			String shortKey = this.getDatabaseFieldnameForConfigEntry(key);
			sqlMainTable.append("`" + shortKey + "` VARCHAR(1000) NOT NULL,");
			keyFieldsSB.append("`" + shortKey + "`,");
		}
		sqlMainTable.append("`" + FIELD_NUMCPUS + "` int(2) NOT NULL,");
		sqlMainTable.append("`" + FIELD_MEMORY + "_max` int(6) NOT NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
		sqlMainTable.append("`" + FIELD_HOST + "` varchar(255) NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_started` TIMESTAMP NULL,");

		/* add columns for result fields */
		for (String result : this.resultFields) {
			sqlMainTable.append("`" + result + "` VARCHAR(500) NULL,");
			if (this.config.getFieldsForWhichToIgnoreTime() == null || !this.config.getFieldsForWhichToIgnoreTime().contains(result)) {
				sqlMainTable.append("`" + result + "_" + FIELD_TIME + "` TIMESTAMP NULL,");
			}
			if (this.config.getFieldsForWhichToIgnoreMemory() == null || !this.config.getFieldsForWhichToIgnoreMemory().contains(result)) {
				sqlMainTable.append("`" + result + "_" + FIELD_MEMORY + "` int(6) NULL,");
			}
		}

		/* exception field and keys */
		sqlMainTable.append("`exception` TEXT NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_end` TIMESTAMP NULL,");
		sqlMainTable.append("PRIMARY KEY (`" + FIELD_ID + "`)");
		sqlMainTable.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		return sqlMainTable.toString();
	}

	@Override
	public void setup(final IExperimentSetConfig config) throws ExperimentDBInteractionFailedException {
		this.config = config;
		this.keyFields = config.getKeyFields().toArray(new String[] {}); // redundant to increase performance
		this.resultFields = config.getResultFields().toArray(new String[] {}); // redundant to increase performance

		/* creates basic table creation statement */
		String createTableQuery = this.getSetupCreateTableQuery();
		try {
			this.adapter.update(createTableQuery, new String[] {});
		} catch (SQLException e) {
			this.logger.error("An SQL exception occured with the following query: {}", createTableQuery);
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	protected String getSQLPrefixForKeySelectQuery() {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT `" + FIELD_ID + "`, `" + FIELD_MEMORY_MAX + "`, `" + FIELD_NUMCPUS + "`, " + Arrays.stream(this.keyFields).collect(Collectors.joining(", ")) + " FROM `");
		queryStringSB.append(this.tablename);
		queryStringSB.append("` ");
		return queryStringSB.toString();
	}

	protected String getSQLPrefixForSelectQuery() {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM `");
		queryStringSB.append(this.tablename);
		queryStringSB.append("` ");
		return queryStringSB.toString();
	}

	@Override
	public Collection<String> getConsideredValuesForKey(final String key) throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.keyFields == null) {
			throw new IllegalStateException(ERROR_NOSETUP);
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT DISTINCT(`" + key + "`) as d FROM ");
		queryStringSB.append(this.tablename);
		try {
			List<IKVStore> res = this.adapter.getRowsOfTable(queryStringSB.toString());
			return res.stream().map(x -> x.getAsString("d")).collect(Collectors.toList());
		} catch (Exception e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public int getNumberOfAllExperiments() throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.keyFields == null) {
			throw new IllegalStateException(ERROR_NOSETUP);
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT COUNT(*) as c FROM ");
		queryStringSB.append(this.tablename);

		try {
			List<IKVStore> res = this.adapter.getResultsOfQuery(queryStringSB.toString());
			return res.get(0).getAsInt("c");
		} catch (Exception e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getAllExperiments() throws ExperimentDBInteractionFailedException {
		try {
			return this.getExperimentsForSQLQuery(this.getSQLPrefixForSelectQuery());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getOpenExperiments() throws ExperimentDBInteractionFailedException {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append(this.getSQLPrefixForKeySelectQuery());
		queryStringSB.append("WHERE time_started IS NULL");
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getRandomOpenExperiments(final int limit) throws ExperimentDBInteractionFailedException {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append(this.getSQLPrefixForKeySelectQuery());
		queryStringSB.append("WHERE time_started IS NULL");
		queryStringSB.append(" ORDER BY RAND() LIMIT " + limit);
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getRunningExperiments() throws ExperimentDBInteractionFailedException {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append(this.getSQLPrefixForKeySelectQuery());
		queryStringSB.append("WHERE time_started IS NOT NULL AND time_end IS NULL");
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getConductedExperiments() throws ExperimentDBInteractionFailedException {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append(this.getSQLPrefixForSelectQuery());
		queryStringSB.append("WHERE time_started IS NOT NULL");
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException("Given query was:\n" + queryStringSB.toString(), e);
		}
	}

	protected List<ExperimentDBEntry> getExperimentsForSQLQuery(final String sql) throws SQLException {
		if (this.config == null || this.keyFields == null) {
			throw new IllegalStateException(ERROR_NOSETUP);
		}
		this.logger.debug("Executing query {}", sql);

		List<IKVStore> res = this.adapter.getResultsOfQuery(sql);
		this.logger.debug("Obtained results, now building experiment objects.");
		List<ExperimentDBEntry> experimentEntries = new ArrayList<>();

		int i = 0;
		long startAll = System.currentTimeMillis();
		for (IKVStore store : res) {
			long start = System.currentTimeMillis();
			/* get key values for experiment */
			Map<String, String> keyValues = new HashMap<>();
			for (String key : this.keyFields) {
				String dbKey = this.getDatabaseFieldnameForConfigEntry(key);
				keyValues.put(dbKey, store.getAsString(dbKey));
			}

			/* get result values for experiment */
			Map<String, Object> resultValues = new HashMap<>();
			for (String key : this.resultFields) {
				String dbKey = this.getDatabaseFieldnameForConfigEntry(key);
				resultValues.put(dbKey, store.getAsString(dbKey));
			}

			ExperimentDBEntry entry = new ExperimentDBEntry(store.getAsInt(FIELD_ID), new Experiment(store.getAsInt(FIELD_MEMORY_MAX), store.getAsInt(FIELD_NUMCPUS), keyValues, resultValues));

			this.logger.trace("Building {}-th object took {}ms.", ++i, System.currentTimeMillis() - start);
			experimentEntries.add(entry);
			if (i % 1000 == 0) {
				this.logger.debug("{} objects have been built within {}ms.", i, System.currentTimeMillis() - startAll);
			}
		}
		return experimentEntries;
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
			valuesToInsert.put(FIELD_MEMORY_MAX, experiment.getMemoryInMB());
			valuesToInsert.put(FIELD_NUMCPUS, experiment.getNumCPUs());
			this.logger.debug("Inserting mem: {}, cpus: {}, host: {}, and key fields: {}", experiment.getMemoryInMB(), experiment.getNumCPUs(), valuesToInsert.get(FIELD_HOST), experiment.getValuesOfKeyFields());
			int id = this.adapter.insert(this.tablename, valuesToInsert)[0];
			return new ExperimentDBEntry(id, experiment);
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public List<ExperimentDBEntry> createAndGetExperiments(final List<Experiment> experiments) throws ExperimentDBInteractionFailedException, ExperimentAlreadyExistsInDatabaseException {
		if (experiments == null || experiments.isEmpty()) {
			throw new IllegalArgumentException();
		}

		/* derive input for insertion */
		List<String> keys = new ArrayList<>();
		keys.add(FIELD_MEMORY_MAX);
		keys.add(FIELD_NUMCPUS);
		keys.addAll(Arrays.asList(this.keyFields));

		List<List<?>> values = new ArrayList<>();
		for (Experiment exp : experiments) {
			List<String> datarow = new ArrayList<>(keys.size());
			datarow.add("" + exp.getMemoryInMB());
			datarow.add("" + exp.getNumCPUs());
			for (String key : this.keyFields) {
				datarow.add(exp.getValuesOfKeyFields().get(key));
			}
			values.add(datarow);
		}

		/* conduct insertion */
		try {
			this.logger.debug("Inserting {} entries", values.size());
			int[] ids = this.adapter.insertMultiple(this.tablename, keys, values);
			this.logger.debug("Inserted {} entries", ids.length);
			int n = ids.length;
			List<ExperimentDBEntry> entries = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				entries.add(new ExperimentDBEntry(ids[i], experiments.get(i)));
			}
			return entries;
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public void updateExperiment(final ExperimentDBEntry exp, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException {
		this.updateExperimentConditionally(exp, new HashMap<>(), values);
	}

	@Override
	public boolean updateExperimentConditionally(final ExperimentDBEntry exp, final Map<String, String> conditions, final Map<String, ? extends Object> values) throws ExperimentUpdateFailedException {
		if (values == null || values.isEmpty()) {
			throw new IllegalArgumentException("No values provided for experiment update of experiment " + exp);
		}
		Collection<String> resultFieldsAsList = Arrays.asList(this.resultFields);
		Collection<String> writableFields = new ArrayList<>(resultFieldsAsList);
		writableFields.add(FIELD_HOST);
		writableFields.add(FIELD_TIME_START);
		writableFields.add("exception");
		writableFields.add(FIELD_TIME_END);
		if (!writableFields.containsAll(values.keySet())) {
			throw new IllegalArgumentException("The value set contains non-result fields: " + SetUtil.difference(values.keySet(), writableFields));
		}
		String now = new SimpleDateFormat(DATE_FORMAT).format(new Date());
		String memoryUsageInMB = String.valueOf((int) Runtime.getRuntime().totalMemory() / 1024 / 1024);
		Map<String, String> valuesToWrite = new HashMap<>();
		values.keySet().forEach(k -> valuesToWrite.put(k, values.get(k).toString()));
		for (String result : values.keySet()) {
			if (resultFieldsAsList.contains(result)) {
				if (this.config.getFieldsForWhichToIgnoreTime() == null || !this.config.getFieldsForWhichToIgnoreTime().contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_TIME, now);
				}
				if (this.config.getFieldsForWhichToIgnoreMemory() == null || !this.config.getFieldsForWhichToIgnoreMemory().contains(result)) {
					valuesToWrite.put(result + "_" + FIELD_MEMORY, memoryUsageInMB);
				}
			}
		}
		Map<String, String> where = new HashMap<>();
		where.putAll(conditions);
		where.put(FIELD_ID, String.valueOf(exp.getId()));
		try {
			return this.adapter.update(this.tablename, valuesToWrite, where) >= 1;
		} catch (SQLException e) {
			throw new ExperimentUpdateFailedException(e);
		}
	}

	@Override
	public void finishExperiment(final ExperimentDBEntry expEntry, final Throwable error) throws ExperimentDBInteractionFailedException {
		Map<String, Object> valuesToAddAfterRun = new HashMap<>();
		if (error != null) {
			valuesToAddAfterRun.put("exception", LoggerUtil.getExceptionInfo(error));
		}
		valuesToAddAfterRun.put(FIELD_TIME_END, new SimpleDateFormat(DATE_FORMAT).format(new Date()));
		this.updateExperiment(expEntry, valuesToAddAfterRun);
	}

	@Override
	public void finishExperiment(final ExperimentDBEntry expEntry) throws ExperimentDBInteractionFailedException {
		this.finishExperiment(expEntry, null);
	}

	protected String getDatabaseFieldnameForConfigEntry(final String configKey) {
		return configKey.replace("\\.", "_");
	}

	@Override
	public void deleteExperiment(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException {
		String deleteExperimentQuery = "DELETE FROM `" + this.tablename + "` WHERE experiment_id = " + exp.getId();
		try {
			this.adapter.query(deleteExperimentQuery);
		} catch (Exception e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public void deleteDatabase() throws ExperimentDBInteractionFailedException {
		String dropTableQuery = "DROP TABLE `" + this.tablename + "`";
		try {
			this.adapter.query(dropTableQuery);
		} catch (Exception e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public void startExperiment(final ExperimentDBEntry exp) throws ExperimentUpdateFailedException, ExperimentAlreadyStartedException {
		Map<String, Object> initValues = new HashMap<>();
		initValues.put(FIELD_TIME_START, new SimpleDateFormat(DATE_FORMAT).format(new Date()));
		try {
			initValues.put(FIELD_HOST, InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			throw new ExperimentUpdateFailedException(e);
		}
		Map<String, String> condition = new HashMap<>();
		condition.put(FIELD_TIME_START, null);
		if (!this.updateExperimentConditionally(exp, condition, initValues)) {
			throw new ExperimentAlreadyStartedException();
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.adapter.setLoggerName(name + ".adapter");
	}

	@Override
	public ExperimentDBEntry getExperimentWithId(final int id) throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.keyFields == null) {
			throw new IllegalStateException(ERROR_NOSETUP);
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM `");
		queryStringSB.append(this.tablename);
		queryStringSB.append("` WHERE `experiment_id` = " + id);
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString()).get(0);
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}
}
