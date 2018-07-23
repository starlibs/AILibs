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

import org.apache.commons.lang3.math.NumberUtils;

/**
 * This is a simple util class for easy database access and query execution in
 * sql. You need to make sure that the respective JDBC connector is in the class
 * path. By default, the adapter uses the mysql driver, but any jdbc driver can
 * be used.
 *
 * @author fmohr
 *
 */
@SuppressWarnings("serial")
public class SQLAdapter implements Serializable, AutoCloseable {
	private final String driver, host, user, password, database;
	private Connection connect;
	private long timestampOfLastAction = Long.MIN_VALUE;
	private final Properties connectionProperties;

	public SQLAdapter(final String host, final String user, final String password, final String database) {
		this("mysql", host, user, password, database, new Properties());
	}

	public SQLAdapter(final String driver, final String host, final String user, final String password,
			final String database, final Properties connectionProperties) {
		super();
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
				// TODO ONLY FOR DEBUGGINH SSL DISABLED
				this.connect = DriverManager
						.getConnection(
								"jdbc:" + this.driver + "://" + this.host + "/" + this.database
										+ "?verifyServerCertificate=false&requireSSL=true&useSSL=true",
								connectionProps);
				return;
			} catch (SQLException e) {
				tries++;
				System.err.println("Connection to server " + this.host + " failed with JDBC driver " + this.driver
						+ " (attempt " + tries + " of 3), waiting 3 seconds and trying again.");
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

	public int insert(final String sql, final List<String> values) throws SQLException {
		this.checkConnection();
		PreparedStatement stmt = this.connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 1; i <= values.size(); i++) {
			String val = values.get(i - 1);
			if (NumberUtils.isCreatable(val)) {
				if (val.contains(".")) {
					stmt.setDouble(i, NumberUtils.toDouble(val));
				} else {
					stmt.setInt(i, NumberUtils.toInt(val));
				}
			} else {
				stmt.setString(i, values.get(i - 1));
			}
		}
		stmt.executeUpdate();
		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}

	public int insert(final String table, final Map<String, ? extends Object> map) throws SQLException {
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
		return this.insert(statement, values);
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

	public void update(final String table, final Map<String, ? extends Object> updateValues,
			final Map<String, ? extends Object> conditions) throws SQLException {
		this.checkConnection();
		StringBuilder updateSB = new StringBuilder();
		List<String> values = new ArrayList<>();
		for (String key : updateValues.keySet()) {
			if (updateSB.length() > 0) {
				updateSB.append(", ");
			}
			updateSB.append(key + " = (?)");
			values.add(updateValues.get(key).toString());
		}

		StringBuilder conditionSB = new StringBuilder();
		for (String key : conditions.keySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(" AND ");
			}
			conditionSB.append(key + " = (?)");
			values.add(conditions.get(key).toString());
		}

		String sql = "UPDATE " + table + " SET " + updateSB.toString() + " WHERE " + conditionSB.toString();
		PreparedStatement stmt = this.connect.prepareStatement(sql);
		for (int i = 1; i <= values.size(); i++) {
			stmt.setString(i, values.get(i - 1));
		}
		stmt.executeUpdate();
	}

	/**
	 * Close the connection. No more queries can be sent after having the access
	 * object closed
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