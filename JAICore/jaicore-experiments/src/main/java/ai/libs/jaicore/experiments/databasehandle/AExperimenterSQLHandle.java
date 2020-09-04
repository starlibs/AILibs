package ai.libs.jaicore.experiments.databasehandle;

import java.io.IOException;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.db.sql.SQLAdapter;
import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.ExperimentSetAnalyzer;
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
	public static final String FIELD_HOST = "host";
	public static final String FIELD_EXECUTOR = "executor";
	public static final String FIELD_NUMCPUS = "cpus";
	private static final String FIELD_TIME = "time";
	private static final String FIELD_TIME_START = FIELD_TIME + "_started";
	private static final String FIELD_TIME_END = FIELD_TIME + "_end";
	private static final String FIELD_EXCEPTION = "exception";
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String Q_AND = " AND ";
	private static final String Q_FROM = " FROM ";

	private final String cachedHost;

	{
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			this.logger.error("Couldn't retrieve Host name. No experiment can be started.", e);
			hostName = null;
		}
		this.cachedHost = hostName;
	}

	protected final IDatabaseAdapter adapter;

	protected final String tablename;

	private IExperimentSetConfig config;

	private ExperimentSetAnalyzer analyzer;
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
			Pair<String, String> decomposition = this.analyzer.getNameTypeSplitForAttribute(key);
			String fieldName = decomposition.getX();
			String fieldType = decomposition.getY();
			if (fieldType == null) {
				fieldType = "varchar(500)";
				this.logger.warn("No type definition given for field {}. Using varchar(500)", fieldName);
			}
			sqlMainTable.append("`" + fieldName + "` " + fieldType + " NOT NULL,");
			keyFieldsSB.append("`" + fieldName + "`,");
		}
		sqlMainTable.append("`" + FIELD_NUMCPUS + "` int(2) NOT NULL,");
		sqlMainTable.append("`" + FIELD_MEMORY + "_max` int(6) NOT NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
		sqlMainTable.append("`" + FIELD_HOST + "` varchar(255) NULL,");
		sqlMainTable.append("`" + FIELD_EXECUTOR + "` varchar(100) NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_started` TIMESTAMP NULL,");

		/* add columns for result fields */
		List<String> fieldsForWhichToIgnoreTime = this.config.getFieldsForWhichToIgnoreTime();
		List<String> fieldsForWhichToIgnoreMemory = this.config.getFieldsForWhichToIgnoreMemory();
		for (String result : this.resultFields) {
			Pair<String, String> decomposition = this.analyzer.getNameTypeSplitForAttribute(result);
			String fieldName = decomposition.getX();
			String fieldType = decomposition.getY();
			if (fieldType == null) {
				fieldType = "varchar(500)";
				this.logger.warn("No type definition given for field {}. Using varchar(500)", fieldName);
			}
			sqlMainTable.append("`" + fieldName + "` " + fieldType + " NULL,");
			if (this.config.getFieldsForWhichToIgnoreTime() == null || fieldsForWhichToIgnoreTime.stream().noneMatch(fieldName::matches)) {
				sqlMainTable.append("`" + fieldName + "_" + FIELD_TIME + "` TIMESTAMP NULL,");
			}
			if (this.config.getFieldsForWhichToIgnoreMemory() == null || fieldsForWhichToIgnoreMemory.stream().noneMatch(fieldName::matches)) {
				sqlMainTable.append("`" + fieldName + "_" + FIELD_MEMORY + "` int(6) NULL,");
			}
		}

		/* exception field and keys */
		sqlMainTable.append("`exception` TEXT NULL,");
		sqlMainTable.append("`" + FIELD_TIME + "_end` TIMESTAMP NULL,");
		sqlMainTable.append("PRIMARY KEY (`" + FIELD_ID + "`)");
		sqlMainTable.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		return sqlMainTable.toString();
	}

	/**
	 * Checks if this instance has been configured.
	 * That is it throws an exception iff the setup method hasn't been successfully called yet.
	 * @throws IllegalStateException thrown if setup wasn't called.
	 */
	protected void assertSetup() {
		if (this.config == null || this.keyFields == null) {
			throw new IllegalStateException(ERROR_NOSETUP);
		}
	}

	@Override
	public void setup(final IExperimentSetConfig config) throws ExperimentDBInteractionFailedException {
		if (this.config != null) {
			if (this.config.equals(config)) {
				this.logger.info("Setup was called repeatedly with the same configuration. Ignoring the subsequent call.", new IllegalStateException());
				return;
			} else {
				throw new IllegalStateException("Setup was called a second time with an alternative experiment set configuration.");
			}
		}
		this.logger.info("Setting up the experiment table {}", this.tablename);
		this.config = config;
		this.analyzer = new ExperimentSetAnalyzer(this.config);
		this.keyFields = config.getKeyFields().toArray(new String[] {}); // redundant to increase performance
		this.resultFields = config.getResultFields().toArray(new String[] {}); // redundant to increase performance

		/* creates basic table creation statement */
		String createTableQuery = this.getSetupCreateTableQuery();
		try {
			this.logger.debug("Sending table creation query: {}", createTableQuery);
			this.adapter.update(createTableQuery, new String[] {});
		} catch (SQLException e) {
			this.logger.error("An SQL exception occured with the following query: {}", createTableQuery);
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	private String buildWhereClause(final Map<String, ?> map) {
		return map.entrySet().stream().map(e -> "`" + e.getKey() + "` = '" + e.getValue().toString() + "'").collect(Collectors.joining(Q_AND));
	}

	protected String getSQLPrefixForKeySelectQuery() {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT `" + FIELD_ID + "`, `" + FIELD_MEMORY_MAX + "`, `" + FIELD_NUMCPUS + "`, " + Arrays.stream(this.keyFields).map(this::getDatabaseFieldnameForConfigEntry).collect(Collectors.joining(", ")));
		queryStringSB.append(this.getSQLFromTable());
		return queryStringSB.toString();
	}

	protected String getSQLFromTable() {
		return Q_FROM + " `" + this.tablename + "` ";
	}

	protected String getSQLPrefixForSelectQuery() {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * ");
		queryStringSB.append(this.getSQLFromTable());
		return queryStringSB.toString();
	}

	@Override
	public Collection<String> getConsideredValuesForKey(final String key) throws ExperimentDBInteractionFailedException {
		if (this.config == null || this.keyFields == null) {
			throw new IllegalStateException(ERROR_NOSETUP);
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT DISTINCT(`" + key + "`) as d ");
		queryStringSB.append(this.getSQLFromTable());
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
		queryStringSB.append("SELECT COUNT(*) as c ");
		queryStringSB.append(this.getSQLFromTable());

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
	public List<ExperimentDBEntry> getRandomOpenExperiments(int limit) throws ExperimentDBInteractionFailedException {
		if (limit == -1) {
			// select a feasible limit:
			limit = 10;
		}
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * FROM ("); // open sub-query
		queryStringSB.append(this.getSQLPrefixForKeySelectQuery());
		queryStringSB.append("WHERE time_started IS NULL LIMIT " + (limit * 100)); // basic pool
		queryStringSB.append(") as t ORDER BY RAND() LIMIT " + limit); // close sub-query and order results
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public Optional<ExperimentDBEntry> startNextExperiment(final String executorInfo) throws ExperimentDBInteractionFailedException {
		if (this.cachedHost == null) {
			// failed to retrieve host information.
			throw new ExperimentUpdateFailedException(new IllegalStateException("Host information is unavailable."));
		}

		/*
		 * Build a query to update a random unstarted experiment and fetch the updated id:
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(this.tablename);
		sb.append(" AS target_table ");

		// Fields to be updated are `time_started`=(now) and `host`=(host name of this machine):
		sb.append("SET target_table.");
		sb.append(FIELD_TIME_START);
		sb.append(" = '");
		sb.append(new SimpleDateFormat(DATE_FORMAT).format(new Date()));
		sb.append("', target_table.");
		sb.append(FIELD_HOST);
		sb.append(" = '");
		sb.append(this.cachedHost);
		sb.append("', target_table.");
		sb.append(FIELD_EXECUTOR);
		sb.append(" = '");
		sb.append(executorInfo);
		sb.append("' ");

		// We need the id of the updated row.
		// The trick is to tell mysql to update the last insert id with the single affected row:
		// See: https://stackoverflow.com/questions/1388025
		// This is a MySQL specific solution.
		sb.append(" WHERE target_table.time_started IS NULL" + " AND last_insert_id(target_table.experiment_id)" + " LIMIT 1");

		int startedExperimentId;
		try {
			// Update and get the affected rows
			// Use insert because we are interested in the `last_insert_id` field that is returned as a generated key.
			int[] affectedKeys = this.adapter.insert(sb.toString(), new String[0]);
			if (affectedKeys == null) {
				throw new IllegalStateException("The database adapter did not return the id of the updated experiment. The sql query executed was: \n" + sb.toString());
			} else if (affectedKeys.length > 1) {
				throw new IllegalStateException("BUG: The sql query affected more than one row. It is supposed to only update a single row: \n" + sb.toString());
			} else if (affectedKeys.length == 0) {
				this.logger.info("No experiment with time_started=null could be found. So no experiment could be started.");
				return Optional.empty();
			} else {
				startedExperimentId = affectedKeys[0];
			}
		} catch (Exception ex) {
			throw new ExperimentDBInteractionFailedException(ex);
		}
		ExperimentDBEntry experimentWithId = this.getExperimentWithId(startedExperimentId);
		if (experimentWithId == null) {
			throw new ExperimentDBInteractionFailedException(new RuntimeException(String.format("BUG: The updated experiment with id, `%d`, could not be fetched. ", startedExperimentId)));
		}
		return Optional.of(experimentWithId);
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
		return this.getConductedExperiments(new HashMap<>());
	}

	@Override
	public List<ExperimentDBEntry> getConductedExperiments(final Map<String, Object> fieldFilter) throws ExperimentDBInteractionFailedException {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append(this.getSQLPrefixForSelectQuery());
		queryStringSB.append("WHERE time_started IS NOT NULL");
		if (!fieldFilter.isEmpty()) {
			queryStringSB.append(Q_AND);
			queryStringSB.append(this.buildWhereClause(fieldFilter));
		}
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString());
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException("Given query was:\n" + queryStringSB.toString(), e);
		}
	}

	@Override
	public List<ExperimentDBEntry> getFailedExperiments() throws ExperimentDBInteractionFailedException {
		return this.getFailedExperiments(new HashMap<>());
	}

	@Override
	public List<ExperimentDBEntry> getFailedExperiments(final Map<String, Object> fieldFilter) throws ExperimentDBInteractionFailedException {
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append(
				"SELECT " + FIELD_ID + ", " + FIELD_MEMORY_MAX + ", " + FIELD_NUMCPUS + ", " + Arrays.stream(this.keyFields).map(k -> this.analyzer.getNameTypeSplitForAttribute(k).getX()).collect(Collectors.joining(", ")) + ", exception");
		queryStringSB.append(this.getSQLFromTable());
		queryStringSB.append("WHERE exception IS NOT NULL");
		if (!fieldFilter.isEmpty()) {
			queryStringSB.append(Q_AND);
			queryStringSB.append(this.buildWhereClause(fieldFilter));
		}
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
			String error = store.getAsString(FIELD_EXCEPTION);
			ExperimentDBEntry entry = new ExperimentDBEntry(store.getAsInt(FIELD_ID), new Experiment(store.getAsInt(FIELD_MEMORY_MAX), store.getAsInt(FIELD_NUMCPUS), keyValues, resultValues, error));

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
	public List<ExperimentDBEntry> createOrGetExperiments(final List<Experiment> experiments) throws ExperimentDBInteractionFailedException {
		Objects.requireNonNull(this.keyFields, "No key fields set!");
		if (experiments == null || experiments.isEmpty()) {
			throw new IllegalArgumentException();
		}

		/* derive input for insertion */
		List<String> keys = new ArrayList<>();
		keys.add(FIELD_MEMORY_MAX);
		keys.add(FIELD_NUMCPUS);
		keys.addAll(Arrays.stream(this.keyFields).map(this::getDatabaseFieldnameForConfigEntry).collect(Collectors.toList()));

		List<List<?>> values = new ArrayList<>();
		for (Experiment exp : experiments) {
			List<String> datarow = new ArrayList<>(keys.size());
			datarow.add("" + exp.getMemoryInMB());
			datarow.add("" + exp.getNumCPUs());
			Map<String, String> expKeys = exp.getValuesOfKeyFields();
			for (String key : this.keyFields) {
				datarow.add(expKeys.get(this.getDatabaseFieldnameForConfigEntry(key)));
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
		Collection<String> resultFieldsAsList = Arrays.stream(this.resultFields).map(k -> this.analyzer.getNameTypeSplitForAttribute(k).getX()).collect(Collectors.toList());
		Collection<String> writableFields = new ArrayList<>(resultFieldsAsList);
		writableFields.add(FIELD_HOST);
		writableFields.add(FIELD_EXECUTOR);
		writableFields.add(FIELD_TIME_START);
		writableFields.add(FIELD_EXCEPTION);
		writableFields.add(FIELD_TIME_END);
		if (!writableFields.containsAll(values.keySet())) {
			throw new IllegalArgumentException("The value set contains non-result fields: " + SetUtil.difference(values.keySet(), writableFields));
		}
		String now = new SimpleDateFormat(DATE_FORMAT).format(new Date());
		String memoryUsageInMB = String.valueOf((int) Runtime.getRuntime().totalMemory() / 1024 / 1024);
		Map<String, String> valuesToWrite = new HashMap<>();
		values.keySet().forEach(k -> valuesToWrite.put(k, values.get(k) != null ? values.get(k).toString() : null));
		List<String> fieldsForWhichToIgnoreTime = this.config.getFieldsForWhichToIgnoreTime();
		List<String> fieldsForWhichToIgnoreMemory = this.config.getFieldsForWhichToIgnoreMemory();
		for (String result : values.keySet()) {
			if (resultFieldsAsList.contains(result)) {
				if (this.config.getFieldsForWhichToIgnoreTime() == null || fieldsForWhichToIgnoreTime.stream().noneMatch(result::matches)) {
					valuesToWrite.put(result + "_" + FIELD_TIME, now);
				}
				if (this.config.getFieldsForWhichToIgnoreMemory() == null || fieldsForWhichToIgnoreMemory.stream().noneMatch(result::matches)) {
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
			throw new ExperimentUpdateFailedException("Could not update " + this.tablename + " with values " + valuesToWrite, e);
		}
	}

	@Override
	public void finishExperiment(final ExperimentDBEntry expEntry, final Throwable error) throws ExperimentDBInteractionFailedException {
		Map<String, Object> valuesToAddAfterRun = new HashMap<>();
		if (error != null) {
			valuesToAddAfterRun.put(FIELD_EXCEPTION, LoggerUtil.getExceptionInfo(error));
		}
		valuesToAddAfterRun.put(FIELD_TIME_END, new SimpleDateFormat(DATE_FORMAT).format(new Date()));
		this.updateExperiment(expEntry, valuesToAddAfterRun);
	}

	@Override
	public void finishExperiment(final ExperimentDBEntry expEntry) throws ExperimentDBInteractionFailedException {
		this.finishExperiment(expEntry, null);
	}

	protected String getDatabaseFieldnameForConfigEntry(final String configKey) {
		return this.analyzer.getNameTypeSplitForAttribute(configKey).getX().replace("\\.", "_");
	}

	@Override
	public void deleteExperiment(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException {
		String deleteExperimentQuery = "DELETE " + this.getSQLFromTable() + " WHERE experiment_id = " + exp.getId();
		try {
			this.adapter.update(deleteExperimentQuery);
		} catch (Exception e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public void deleteDatabase() throws ExperimentDBInteractionFailedException {
		String dropTableQuery = "DROP TABLE `" + this.tablename + "`";
		try {
			this.adapter.insert(dropTableQuery, new String[0]);
		} catch (Exception e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}

	@Override
	public boolean hasExperimentStarted(final ExperimentDBEntry exp) throws ExperimentDBInteractionFailedException {
		this.assertSetup();
		StringBuilder queryStringSB = new StringBuilder();
		int expId = exp.getId();
		queryStringSB.append("SELECT ").append(FIELD_TIME_START);
		queryStringSB.append(this.getSQLFromTable());
		queryStringSB.append(" WHERE `experiment_id` = ").append(expId);
		try {
			List<IKVStore> selectResult = this.adapter.query(queryStringSB.toString());
			if (selectResult.isEmpty()) {
				throw new IllegalArgumentException("The given experiment was not found: " + exp);
			}
			if (selectResult.size() > 1) {
				throw new IllegalStateException("The experiment with primary id " + exp.getId() + " exists multiple times.");
			}
			IKVStore selectedRow = selectResult.get(0);
			if (selectedRow.get(FIELD_TIME_START) != null) {
				return true;
			}
		} catch (SQLException | IOException ex) {
			throw new ExperimentDBInteractionFailedException(ex);
		}
		return false;
	}

	@Override
	public void startExperiment(final ExperimentDBEntry exp, final String executorInfo) throws ExperimentUpdateFailedException, ExperimentAlreadyStartedException {
		this.assertSetup();
		Map<String, Object> initValues = new HashMap<>();
		initValues.put(FIELD_TIME_START, new SimpleDateFormat(DATE_FORMAT).format(new Date()));
		initValues.put(FIELD_HOST, this.cachedHost);
		initValues.put(FIELD_EXECUTOR, executorInfo);
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
		this.assertSetup();
		StringBuilder queryStringSB = new StringBuilder();
		queryStringSB.append("SELECT * ");
		queryStringSB.append(this.getSQLFromTable());
		queryStringSB.append(" WHERE `experiment_id` = " + id);
		try {
			return this.getExperimentsForSQLQuery(queryStringSB.toString()).get(0);
		} catch (SQLException e) {
			throw new ExperimentDBInteractionFailedException(e);
		}
	}
}
