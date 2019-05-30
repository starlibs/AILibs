package jaicore.basic;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;

/**
 * This is a simple util class for easy database access and query execution in sql. You need to make sure that the respective JDBC connector is in the class path. By default, the adapter uses the mysql driver, but any jdbc driver can be
 * used.
 *
 * @author fmohr
 *
 */
@SuppressWarnings("serial")
public class SQLAdapter implements Serializable, AutoCloseable {

	private transient Logger logger = LoggerFactory.getLogger(SQLAdapter.class);

	private static final String DB_DRIVER = "mysql";
	private static final String KEY_EQUALS_VALUE_TO_BE_SET = " = (?)";

	/* Credentials and properties for the connection establishment. */
	private final String driver;
	private final String host;
	private final String user;
	private final String password;
	private final String database;
	private final boolean ssl;
	private final Properties connectionProperties;

	/* Connection object */
	private transient Connection connect;

	private long timestampOfLastAction = Long.MIN_VALUE;

	public SQLAdapter(final String host, final String user, final String password, final String database, final boolean ssl) {
		this(DB_DRIVER, host, user, password, database, new Properties(), ssl);
	}

	public SQLAdapter(final String host, final String user, final String password, final String database) {
		this(DB_DRIVER, host, user, password, database, new Properties());
	}

	public SQLAdapter(final String driver, final String host, final String user, final String password, final String database, final Properties connectionProperties) {
		this(driver, host, user, password, database, connectionProperties, true);
	}

	public SQLAdapter(final String driver, final String host, final String user, final String password, final String database, final Properties connectionProperties, final boolean ssl) {
		super();
		this.ssl = ssl;
		this.driver = driver;
		this.host = host;
		this.user = user;
		this.password = password;
		this.database = database;
		this.connectionProperties = connectionProperties;

		Runtime.getRuntime().addShutdownHook(new ShutdownThread(SQLAdapter.this));
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
				String connectionString = "jdbc:" + this.driver + "://" + this.host + "/" + this.database + ((this.ssl) ? "?verifyServerCertificate=false&requireSSL=true&useSSL=true" : "");
				this.connect = DriverManager.getConnection(connectionString, connectionProps);
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

	public PreparedStatement getPreparedStatement(final String query) throws SQLException {
		this.checkConnection();
		return this.connect.prepareStatement(query);
	}

	public synchronized void checkConnection() throws SQLException {
		int renewAfterSeconds = 5 * 60;
		if (this.timestampOfLastAction + renewAfterSeconds * 1000 < System.currentTimeMillis()) {
			this.close();
			this.connect();
		}
		this.timestampOfLastAction = System.currentTimeMillis();
	}

	public ResultSet getRowsOfTable(final String table) throws SQLException {
		return this.getRowsOfTable(table, new HashMap<>());
	}

	public ResultSet getRowsOfTable(final String table, final Map<String, String> conditions) throws SQLException {
		StringBuilder conditionSB = new StringBuilder();
		List<String> values = new ArrayList<>();
		for (Entry<String, String> entry : conditions.entrySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(" AND ");
			} else {
				conditionSB.append(" WHERE ");
			}
			conditionSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			values.add(entry.getValue());
		}
		return this.getResultsOfQuery("SELECT * FROM `" + table + "`" + conditionSB.toString(), values);
	}

	public ResultSet getResultsOfQuery(final String query) throws SQLException {
		return this.getResultsOfQuery(query, new ArrayList<>());
	}

	public ResultSet getResultsOfQuery(final String query, final String[] values) throws SQLException {
		return this.getResultsOfQuery(query, Arrays.asList(values));
	}

	public ResultSet getResultsOfQuery(final String query, final List<String> values) throws SQLException {
		this.checkConnection();
		PreparedStatement statement = this.connect.prepareStatement(query);
		for (int i = 1; i <= values.size(); i++) {
			statement.setString(i, values.get(i - 1));
		}
		return statement.executeQuery();
	}

	public int insert(final String sql, final String[] values) throws SQLException {
		return this.insert(sql, Arrays.asList(values));
	}

	public int insert(final String sql, final List<? extends Object> values) throws SQLException {
		this.checkConnection();
		PreparedStatement stmt = this.connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 1; i <= values.size(); i++) {
			this.setValue(stmt, i, values.get(i - 1));
		}
		stmt.executeUpdate();

		try (ResultSet rs = stmt.getGeneratedKeys()) {
			rs.next();
			return rs.getInt(1);
		}
	}

	public int insert(final String table, final Map<String, ? extends Object> map) throws SQLException {
		Pair<String, List<Object>> insertStatement = this.buildInsertStatement(table, map);
		return this.insert(insertStatement.getX(), insertStatement.getY());
	}

	public void insertNoNewValues(final String sql, final List<? extends Object> values) throws SQLException {
		this.checkConnection();
		PreparedStatement stmt = this.connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 1; i <= values.size(); i++) {
			this.setValue(stmt, i, values.get(i - 1));
		}
		stmt.executeUpdate();
	}

	private Pair<String, List<Object>> buildInsertStatement(final String table, final Map<String, ? extends Object> map) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (Entry<String, ? extends Object> entry : map.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			if (sb1.length() != 0) {
				sb1.append(", ");
				sb2.append(", ");
			}
			sb1.append(entry.getKey());
			sb2.append("?");
			values.add(entry.getValue());
		}

		String statement = "INSERT INTO " + table + " (" + sb1.toString() + ") VALUES (" + sb2.toString() + ")";

		return new Pair<>(statement, values);

	}

	public void insertNoNewValues(final String table, final Map<String, ? extends Object> map) throws SQLException {
		Pair<String, List<Object>> insertStatement = this.buildInsertStatement(table, map);
		this.insertNoNewValues(insertStatement.getX(), insertStatement.getY());
	}

	public void update(final String sql) throws SQLException {
		this.update(sql, new ArrayList<String>());
	}

	public void update(final String sql, final String[] values) throws SQLException {
		this.update(sql, Arrays.asList(values));
	}

	public void update(final String sql, final List<? extends Object> values) throws SQLException {
		this.checkConnection();
		PreparedStatement stmt = this.connect.prepareStatement(sql);
		for (int i = 1; i <= values.size(); i++) {
			stmt.setString(i, values.get(i - 1).toString());
		}
		stmt.executeUpdate();
	}

	public void update(final String table, final Map<String, ? extends Object> updateValues, final Map<String, ? extends Object> conditions) throws SQLException {
		this.checkConnection();
		StringBuilder updateSB = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (Entry<String, ? extends Object> entry : updateValues.entrySet()) {
			if (updateSB.length() > 0) {
				updateSB.append(", ");
			}
			updateSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			values.add(entry.getValue());
		}

		StringBuilder conditionSB = new StringBuilder();
		for (Entry<String, ? extends Object> entry : conditions.entrySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(" AND ");
			}
			conditionSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			values.add(entry.getValue());
		}

		String sql = "UPDATE " + table + " SET " + updateSB.toString() + " WHERE " + conditionSB.toString();
		PreparedStatement stmt = this.connect.prepareStatement(sql);
		for (int i = 1; i <= values.size(); i++) {
			this.setValue(stmt, i, values.get(i - 1));
		}
		stmt.executeUpdate();
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

	private void setValue(final PreparedStatement stmt, final int index, final Object val) throws SQLException {
		if (val instanceof Integer) {
			stmt.setInt(index, (Integer) val);
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

	public String getDriver() {
		return this.driver;
	}
}