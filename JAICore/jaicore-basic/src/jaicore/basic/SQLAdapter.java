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
import java.util.Properties;

/**
 * This is a simple util class for easy database access and query execution in sql. You need to make sure that the respective JDBC connector is in the class path. By default, the adapter uses the mysql driver, but any jdbc driver can be
 * used.
 *
 * @author fmohr
 *
 */
@SuppressWarnings("serial")
public class SQLAdapter implements Serializable, AutoCloseable {
	private final String driver, host, user, password, database;
	private final boolean ssl;
	private Connection connect;
	private long timestampOfLastAction = Long.MIN_VALUE;
	private final Properties connectionProperties;

	public SQLAdapter(final String host, final String user, final String password, final String database, final boolean ssl) {
		this("mysql", host, user, password, database, new Properties(), ssl);
	}

	public SQLAdapter(final String host, final String user, final String password, final String database) {
		this("mysql", host, user, password, database, new Properties());
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

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				SQLAdapter.this.close();
			}
		}));
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
				System.err.println("Connection to server " + this.host + " failed with JDBC driver " + this.driver + " (attempt " + tries + " of 3), waiting 3 seconds and trying again.");
				e.printStackTrace();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		} while (tries < 3);
		System.err.println("Quitting execution");
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
		for (String key : conditions.keySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(" AND ");
			} else {
				conditionSB.append(" WHERE ");
			}
			conditionSB.append(key + " = (?)");
			values.add(conditions.get(key));
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

		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}

	public int insert(final String table, final Map<String, ? extends Object> map) throws SQLException {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (String key : map.keySet()) {
			if (map.get(key) == null) {
				continue;
			}
			if (sb1.length() != 0) {
				sb1.append(", ");
				sb2.append(", ");
			}
			sb1.append(key);
			sb2.append("?");
			values.add(map.get(key));
		}

		String statement = "INSERT INTO " + table + " (" + sb1.toString() + ") VALUES (" + sb2.toString() + ")";
		return this.insert(statement, values);
	}

	public void insert_noAutoGeneratedFields(final String sql, final List<? extends Object> values) throws SQLException {
		this.checkConnection();
		PreparedStatement stmt = this.connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 1; i <= values.size(); i++) {
			this.setValue(stmt, i, values.get(i - 1));
		}
		stmt.executeUpdate();
	}

	public void insert_noAutoGeneratedFields(final String table, final Map<String, ? extends Object> map)
			throws SQLException {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		List<String> values = new ArrayList<>();
		for (String key : map.keySet()) {
			if (map.get(key) == null) {
				continue;
			}
			if (sb1.length() != 0) {
				sb1.append(", ");
				sb2.append(", ");
			}
			sb1.append(key);
			sb2.append("?");
			values.add(map.get(key).toString());
		}

		String statement = "INSERT INTO " + table + " (" + sb1.toString() + ") VALUES (" + sb2.toString() + ")";
		this.insert_noAutoGeneratedFields(statement, values);
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
		for (String key : updateValues.keySet()) {
			if (updateSB.length() > 0) {
				updateSB.append(", ");
			}
			updateSB.append(key + " = (?)");
			values.add(updateValues.get(key));
		}

		StringBuilder conditionSB = new StringBuilder();
		for (String key : conditions.keySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(" AND ");
			}
			conditionSB.append(key + " = (?)");
			values.add(conditions.get(key));
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
			e.printStackTrace();
			System.err.println("Transaction is being rolled back.");
			try {
				this.connect.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
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
			e.printStackTrace();
		}
	}

	public String getDriver() {
		return this.driver;
	}
}