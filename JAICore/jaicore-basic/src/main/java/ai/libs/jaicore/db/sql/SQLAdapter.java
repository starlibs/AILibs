package ai.libs.jaicore.db.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.IDatabaseConfig;

/**
 * This is a simple util class for easy database access and query execution in sql. You need to make sure that the respective JDBC connector is in the class path. By default, the adapter uses the mysql driver, but any jdbc driver can be
 * used.
 *
 * @author fmohr, mwever
 *
 */
@SuppressWarnings("serial")
public class SQLAdapter implements IDatabaseAdapter {

	private transient Logger logger = LoggerFactory.getLogger(SQLAdapter.class);

	private static final String DB_DRIVER = "mysql";
	private static final String KEY_EQUALS_VALUE_TO_BE_SET = " = (?)";

	private static final String STR_SPACE_AND = " AND ";
	private static final String STR_SPACE_WHERE = " WHERE ";

	/* Credentials and properties for the connection establishment. */
	private final String driver;
	private final String host;
	private final String user;
	private final String password;
	private final String database;
	private final boolean ssl;
	private final Properties connectionProperties;
	private final ISQLQueryBuilder queryBuilder = new MySQLQueryBuilder();

	/* Connection object */
	private transient Connection connect;

	private long timestampOfLastAction = Long.MIN_VALUE;

	/**
	 * Standard c'tor.
	 *
	 * @param config The database configuration including a definition of host, user, password, database and whether to connect to the server via SSL.
	 */
	public SQLAdapter(final IDatabaseConfig config) {
		this(DB_DRIVER, config.getDBHost(), config.getDBUsername(), config.getDBPassword(), config.getDBDatabaseName(), new Properties(), config.getDBSSL());
	}

	/**
	 * Constructor for an SQLAdapter.
	 *
	 * @param host The host of the (remote) database server.
	 * @param user The username for logging into the database server.
	 * @param password The password corresponding to the username.
	 * @param database The name of the database to connect to.
	 * @param ssl Flag whether the connection must be ssl encrypted or not.
	 */
	public SQLAdapter(final String host, final String user, final String password, final String database, final boolean ssl) {
		this(DB_DRIVER, host, user, password, database, new Properties(), ssl);
	}

	/**
	 * Constructor for an SQLAdapter. The connection is by default SSL encrypted.
	 *
	 * @param host The host of the (remote) database server.
	 * @param user The username for logging into the database server.
	 * @param password The password corresponding to the username.
	 * @param database The name of the database to connect to.
	 */
	public SQLAdapter(final String host, final String user, final String password, final String database) {
		this(DB_DRIVER, host, user, password, database, new Properties());
	}

	/**
	 * Constructor for an SQLAdapter. The connection is by default SSL encrypted.
	 *
	 * @param host The host of the (remote) database server.
	 * @param user The username for logging into the database server.
	 * @param password The password corresponding to the username.
	 * @param database The name of the database to connect to.
	 * @param connectionProperties In these properties additional properties for the SQL connection may be defined.
	 */
	public SQLAdapter(final String driver, final String host, final String user, final String password, final String database, final Properties connectionProperties) {
		this(driver, host, user, password, database, connectionProperties, true);
	}

	/**
	 * Constructor for an SQLAdapter.
	 *
	 * @param host The host of the (remote) database server.
	 * @param user The username for logging into the database server.
	 * @param password The password corresponding to the username.
	 * @param database The name of the database to connect to.
	 * @param connectionProperties In these properties additional properties for the SQL connection may be defined.
	 * @param ssl Flag whether the connection must be ssl encrypted or not.
	 */
	public SQLAdapter(final String driver, final String host, final String user, final String password, final String database, final Properties connectionProperties, final boolean ssl) {
		super();
		this.ssl = ssl;
		this.driver = driver;
		this.host = host;
		this.user = user;
		this.password = password;
		this.database = database;
		this.connectionProperties = connectionProperties;

		try {
			Runtime.getRuntime().addShutdownHook(new ShutdownThread(SQLAdapter.this));
		} catch (Exception e) {
			this.logger.warn("Failed to add shutdown hook for SQLAdapter");
		}
	}

	/**
	 * Thread shutting down the SQLAdapter in the event of a shutdown of the JVM.
	 *
	 * @author mwever
	 */
	private class ShutdownThread extends Thread {
		private SQLAdapter adapter;

		/**
		 * C'tor that is provided with an SQLAdapter to be shut down.
		 *
		 * @param adapter The SQLAdapter to be shut down.
		 */
		public ShutdownThread(final SQLAdapter adapter) {
			this.adapter = adapter;
		}

		@Override
		public void run() {
			this.adapter.close();
		}
	}

	private void connect() throws SQLException {
		int tries = 0;
		do {
			try {
				Properties connectionProps = new Properties(this.connectionProperties);
				connectionProps.put("user", this.user);
				connectionProps.put("password", this.password);
				String connectionString = "jdbc:" + this.driver + "://" + this.host + "/" + this.database + ((this.ssl) ? "?verifyServerCertificate=false&requireSSL=true&useSSL=true" : "?useSSL=false");
				this.logger.info("Connecting to {}", connectionString);
				this.connect = DriverManager.getConnection(connectionString, connectionProps);
				this.logger.info("Connection established.");
				return;
			} catch (SQLException e) {
				tries++;
				this.logger.error("Connection to server {} failed with JDBC driver {} (attempt {} of 3), waiting 3 seconds before trying again.", this.host, this.driver, tries, e);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					this.logger.error(
							"SQLAdapter got interrupted while trying to establish a connection to the database. NOTE: This will trigger an immediate shutdown as no sql connection could be established. Reason for the interrupt was:", e1);
					break;
				}
			}
		} while (tries < 3);
		this.logger.error("Quitting execution as no database connection could be established");
		System.exit(1);
	}

	/**
	 * Returns a prepared statement for the given query so that any placeholder may be filled into the prepared statement.
	 * @param query The query for which a prepared statement shall be returned.
	 * @return The prepared statement for the given query.
	 * @throws SQLException Thrown, if there was an issue with the connection to the database.
	 */
	public PreparedStatement getPreparedStatement(final String query) throws SQLException {
		this.checkConnection();
		return this.connect.prepareStatement(query);
	}

	/**
	 * Checks whether the connection to the database is still alive and re-establishs the connection if it is not.
	 * @throws SQLException Thrown, if there was an issue with reconnecting to the database server.
	 */
	@Override
	public synchronized void checkConnection() throws SQLException {
		int renewAfterSeconds = 5 * 60;
		if (this.timestampOfLastAction + renewAfterSeconds * 1000 < System.currentTimeMillis()) {
			this.close();
			this.connect();
		}
		this.timestampOfLastAction = System.currentTimeMillis();
	}

	/**
	 * Retrieves all rows of a table.
	 * @param table The table for which all entries shall be returned.
	 * @return A list of {@link IKVStore}s containing the data of the table.
	 * @throws SQLException Thrown, if there was an issue with the connection to the database.
	 */
	@Override
	public List<IKVStore> getRowsOfTable(final String table) throws SQLException {
		this.logger.info("Fetching complete table {}", table);
		return this.getRowsOfTable(table, new HashMap<>());
	}

	/**
	 * Retrieves all rows of a table which satisfy certain conditions (WHERE clause).
	 * @param table The table for which all entries shall be returned.
	 * @param conditions The conditions a result entry must satisfy.
	 * @return A list of {@link IKVStore}s containing the data of the table.
	 * @throws SQLException Thrown, if there was an issue with the connection to the database.
	 */
	@Override
	public List<IKVStore> getRowsOfTable(final String table, final Map<String, String> conditions) throws SQLException {
		return this.getResultsOfQuery(this.queryBuilder.buildSelectSQLCommand(table, conditions));
	}

	public Iterator<IKVStore> getRowIteratorOfTable(final String table) throws SQLException {
		return this.getRowIteratorOfTable(table, new HashMap<>());
	}

	public Iterator<IKVStore> getRowIteratorOfTable(final String table, final Map<String, String> conditions) throws SQLException {
		StringBuilder conditionSB = new StringBuilder();
		List<String> values = new ArrayList<>();
		for (Entry<String, String> entry : conditions.entrySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(STR_SPACE_AND);
			} else {
				conditionSB.append(STR_SPACE_WHERE);
			}
			conditionSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			values.add(entry.getValue());
		}
		return this.getResultIteratorOfQuery("SELECT * FROM `" + table + "`" + conditionSB.toString(), values);
	}

	/**
	 * Retrieves the select result for the given query.
	 * @param query The SQL query which is to be executed.
	 * @return A list of {@link IKVStore}s containing the result data of the query.
	 * @throws SQLException Thrown, if there was an issue with the connection to the database.
	 */
	@Override
	public List<IKVStore> getResultsOfQuery(final String query) throws SQLException {
		return this.getResultsOfQuery(query, new ArrayList<>());
	}

	/**
	 * Retrieves the select result for the given query that can have placeholders.
	 * @param query The SQL query which is to be executed (with placeholders).
	 * @param values An array of placeholder values that need to be filled in.
	 * @return A list of {@link IKVStore}s containing the result data of the query.
	 * @throws SQLException Thrown, if there was an issue with the connection to the database.
	 */
	@Override
	public List<IKVStore> getResultsOfQuery(final String query, final String[] values) throws SQLException {
		return this.getResultsOfQuery(query, Arrays.asList(values));
	}

	/**
	 * Retrieves the select result for the given query that can have placeholders.
	 * @param query The SQL query which is to be executed (with placeholders).
	 * @param values A list of placeholder values that need to be filled in.
	 * @return A list of {@link IKVStore}s containing the result data of the query.
	 * @throws SQLException Thrown, if there was an issue with the query format or the connection to the database.
	 */
	@Override
	public List<IKVStore> getResultsOfQuery(final String query, final List<String> values) throws SQLException {
		this.checkConnection();
		this.logger.info("Conducting query {} with values {}", query, values);
		try (PreparedStatement statement = this.connect.prepareStatement(query)) {
			for (int i = 1; i <= values.size(); i++) {
				statement.setString(i, values.get(i - 1));
			}
			return new ResultSetToKVStoreSerializer().serialize(statement.executeQuery());
		}
	}

	public Iterator<IKVStore> getResultIteratorOfQuery(final String query, final List<String> values) throws SQLException {
		this.checkConnection();
		boolean autoCommit = this.connect.getAutoCommit();
		this.connect.setAutoCommit(false); // deactivate autocommit for this request
		this.logger.info("Conducting query {} with values {}", query, values);
		PreparedStatement statement = this.connect.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(100); // this avoids that the whole result table is read
		for (int i = 1; i <= values.size(); i++) {
			statement.setString(i, values.get(i - 1));
		}
		Iterator<IKVStore> iterator = new ResultSetToKVStoreSerializer().getSerializationIterator(statement.executeQuery());
		this.connect.setAutoCommit(autoCommit);
		return iterator;
	}

	/**
	 * Executes an insert query and returns the row ids of the created entries.
	 * @param sql The insert statement which shall be executed that may have placeholders.
	 * @param values The values for the placeholders.
	 * @return An array of the row ids of the inserted entries.
	 * @throws SQLException Thrown, if there was an issue with the query format or the connection to the database.
	 */
	@Override
	public int[] insert(final String sql, final String[] values) throws SQLException {
		return this.insert(sql, Arrays.asList(values));
	}

	/**
	 * Executes an insert query and returns the row ids of the created entries.
	 * @param sql The insert statement which shall be executed that may have placeholders.
	 * @param values A list of values for the placeholders.
	 * @return An array of the row ids of the inserted entries.
	 * @throws SQLException Thrown, if there was an issue with the query format or the connection to the database.
	 */
	@Override
	public int[] insert(final String sql, final List<? extends Object> values) throws SQLException {
		this.checkConnection();
		try (PreparedStatement stmt = this.connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			for (int i = 1; i <= values.size(); i++) {
				this.setValue(stmt, i, values.get(i - 1));
			}
			stmt.executeUpdate();
			List<Integer> generatedKeys = new LinkedList<>();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				while (rs.next()) {
					generatedKeys.add(rs.getInt(1));
				}
			}
			return generatedKeys.stream().mapToInt(x -> x).toArray();
		}
	}

	/**
	 * Creates and executes an insert query for the given table and the values as specified in the map.
	 * @param table The table where to insert the data.
	 * @param map The map of key:value pairs to be inserted into the table.
	 * @return An array of the row ids of the inserted entries.
	 * @throws SQLException Thrown, if there was an issue with the query format or the connection to the database.
	 */
	@Override
	public int[] insert(final String table, final Map<String, ? extends Object> map) throws SQLException {
		Pair<String, List<Object>> insertStatement = this.queryBuilder.buildInsertStatement(table, map);
		return this.insert(insertStatement.getX(), insertStatement.getY());
	}

	/**
	 * Creates a multi-insert statement and executes it. The returned array contains the row id's of the inserted rows. (By default it creates chunks of size 10.000 rows per query to be inserted.)
	 * @param table The table to which the rows are to be added.
	 * @param keys The list of column keys for which values are set.
	 * @param datarows The list of value lists to be filled into the table.
	 * @return An array of row id's of the inserted rows.
	 * @throws SQLException Thrown, if the sql statement was malformed, could not be executed, or the connection to the database failed.
	 */
	@Override
	public int[] insertMultiple(final String table, final List<String> keys, final List<List<? extends Object>> datarows) throws SQLException {
		return this.insertMultiple(table, keys, datarows, 10000);
	}

	/**
	 * Creates a multi-insert statement and executes it. The returned array contains the row id's of the inserted rows.
	 * @param table The table to which the rows are to be added.
	 * @param keys The list of column keys for which values are set.
	 * @param datarows The list of value lists to be filled into the table.
	 * @param chunkSize The number of rows which are added within one single database transaction. (10,000 seems to be a good value for this)
	 * @return An array of row id's of the inserted rows.
	 * @throws SQLException Thrown, if the sql statement was malformed, could not be executed, or the connection to the database failed.
	 */
	@Override
	public int[] insertMultiple(final String table, final List<String> keys, final List<List<? extends Object>> datarows, final int chunkSize) throws SQLException {
		int n = datarows.size();
		this.checkConnection();
		List<Integer> ids = new ArrayList<>(n);
		try (Statement stmt = this.connect.createStatement()) {
			for (int i = 0; i < Math.ceil(n * 1.0 / chunkSize); i++) {
				int startIndex = i * chunkSize;
				int endIndex = Math.min((i + 1) * chunkSize, n);
				String sql = this.queryBuilder.buildMultiInsertSQLCommand(table, keys, datarows.subList(startIndex, endIndex));
				this.logger.debug("Created SQL for {} entries", endIndex - startIndex);
				this.logger.trace("Adding sql statement {} to batch", sql);
				stmt.addBatch(sql);
			}
			this.logger.debug("Start batch execution.");
			stmt.executeBatch();
			this.logger.debug("Finished batch execution.");
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
			return ids.stream().mapToInt(x -> x).toArray();
		}
	}

	/**
	 * Execute the given sql statement as an update.
	 * @param sql The sql statement to be executed.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	@Override
	public int update(final String sql) throws SQLException {
		return this.update(sql, new ArrayList<>());
	}

	/**
	 * Execute the given sql statement with placeholders as an update filling the placeholders with the given values beforehand.
	 * @param sql The sql statement with placeholders to be executed.
	 * @param sql Array of values for the respective placeholders.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	@Override
	public int update(final String sql, final String[] values) throws SQLException {
		return this.update(sql, Arrays.asList(values));
	}

	/**
	 * Execute the given sql statement with placeholders as an update filling the placeholders with the given values beforehand.
	 * @param sql The sql statement with placeholders to be executed.
	 * @param values List of values for the respective placeholders.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	@Override
	public int update(final String sql, final List<? extends Object> values) throws SQLException {
		this.checkConnection();
		this.logger.debug("Executing update query: {} with values {}", sql, values);
		try (PreparedStatement stmt = this.connect.prepareStatement(sql)) {
			for (int i = 1; i <= values.size(); i++) {
				stmt.setString(i, values.get(i - 1).toString());
			}
			return stmt.executeUpdate();
		}
	}

	/**
	 * Create and execute an update statement for some table updating the values as described in <code>updateValues</code> and only affect those entries satisfying the <code>conditions</code>.
	 * @param table The table which is to be updated.
	 * @param updateValues The description how entries are to be updated.
	 * @param conditions The description of the where-clause, conditioning the entries which are to be updated.
	 * @return The number of rows affected by the update statement.
	 * @throws SQLException Thrown if the statement is malformed or an issue while executing the sql statement occurs.
	 */
	@Override
	public int update(final String table, final Map<String, ? extends Object> updateValues, final Map<String, ? extends Object> conditions) throws SQLException {
		this.checkConnection();

		// build the update mapping.
		StringBuilder updateSB = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (Entry<String, ? extends Object> entry : updateValues.entrySet()) {
			if (updateSB.length() > 0) {
				updateSB.append(", ");
			}
			updateSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			values.add(entry.getValue());
		}

		// build the condition restricting the elements which are affected by the update.
		StringBuilder conditionSB = new StringBuilder();
		for (Entry<String, ? extends Object> entry : conditions.entrySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(STR_SPACE_AND);
			}
			if (entry.getValue() != null) {
				conditionSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
				values.add(entry.getValue());
			} else {
				conditionSB.append(entry.getKey());
				conditionSB.append(" IS NULL");
			}
		}

		// Build query for the update command.
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("UPDATE ");
		sqlBuilder.append(table);
		sqlBuilder.append(" SET ");
		sqlBuilder.append(updateSB.toString());
		sqlBuilder.append(STR_SPACE_WHERE);
		sqlBuilder.append(conditionSB.toString());

		try (PreparedStatement stmt = this.connect.prepareStatement(sqlBuilder.toString())) {
			for (int i = 1; i <= values.size(); i++) {
				this.setValue(stmt, i, values.get(i - 1));
			}
			return stmt.executeUpdate();
		}
	}

	/**
	 * Executes the given statements atomically. Only works if no other statements are sent through this adapter in parallel! Only use for single-threaded applications, otherwise side effects may happen as this changes the auto commit
	 * settings of the connection temporarily.
	 *
	 * @param queries
	 *            The queries to execute atomically
	 * @throws SQLException
	 *             If the status of the connection cannot be changed. If something goes wrong while executing the given statements, they are rolled back before they are committed.
	 */
	@Override
	public void executeQueriesAtomically(final List<PreparedStatement> queries) throws SQLException {
		this.checkConnection();
		this.connect.setAutoCommit(false);

		try {
			for (PreparedStatement query : queries) {
				query.execute();
			}
			this.connect.commit();
		} catch (SQLException e) {
			this.logger.error("Transaction is being rolled back.", e);
			try {
				this.connect.rollback();
			} catch (SQLException e1) {
				this.logger.error("Could not rollback the connection", e1);
			}
		} finally {
			for (PreparedStatement query : queries) {
				if (query != null) {
					query.close();
				}
			}

			this.connect.setAutoCommit(true);
		}
	}

	@Override
	public List<IKVStore> query(final String sqlStatement) throws SQLException, IOException {
		this.checkConnection();
		try (Statement stmt = this.connect.createStatement()) {
			try (ResultSet ps = stmt.executeQuery(sqlStatement)) {
				return new ResultSetToKVStoreSerializer().serialize(ps);
			}
		}
	}

	private void setValue(final PreparedStatement stmt, final int index, final Object val) throws SQLException {
		if (val instanceof Integer) {
			stmt.setInt(index, (Integer) val);
		} else if (val instanceof Long) {
			stmt.setLong(index, (Long) val);
		} else if (val instanceof Number) {
			stmt.setDouble(index, (Double) val);
		} else if (val instanceof String) {
			stmt.setString(index, (String) val);
		} else {
			stmt.setObject(index, val);
		}
	}

	/**
	 * Close the connection. No more queries can be sent after having the access object closed
	 */
	@Override
	public void close() {
		try {
			if (this.connect != null) {
				this.connect.close();
			}
		} catch (Exception e) {
			this.logger.error("An exception occurred while closing the database connection.", e);
		}
	}

	/**
	 * Getter for the sql database driver.
	 * @return The name of the database driver.
	 */
	public String getDriver() {
		return this.driver;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void createTable(final String tablename, final String nameOfPrimaryField, final Collection<String> fieldnames, final Map<String, String> types, final Collection<String> keys) throws SQLException {
		this.checkConnection();
		Objects.requireNonNull(this.connect);
		StringBuilder sqlMainTable = new StringBuilder();
		StringBuilder keyFieldsSB = new StringBuilder();
		sqlMainTable.append("CREATE TABLE IF NOT EXISTS `" + tablename + "` (");
		sqlMainTable.append("`" + nameOfPrimaryField + "` " + types.get(nameOfPrimaryField) + " NOT NULL AUTO_INCREMENT,");
		for (String key : fieldnames) {
			if (!types.containsKey(key)) {
				throw new IllegalArgumentException("No type information given for field " + key);
			}
			sqlMainTable.append("`" + key + "` " + types.get(key) + (types.get(key).contains("NULL") ? "" : " NOT NULL") + ",");
			keyFieldsSB.append("`" + key + "`,");
		}
		sqlMainTable.append("PRIMARY KEY (`" + nameOfPrimaryField + "`)");
		sqlMainTable.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");

		/* prepare statement */
		try (Statement stmt = this.connect.createStatement()) {
			this.logger.info("Executing query: {}", sqlMainTable);
			stmt.execute(sqlMainTable.toString());
		}
	}
}