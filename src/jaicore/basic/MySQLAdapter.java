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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;

@SuppressWarnings("serial")
public class MySQLAdapter implements Serializable {
	private final String driver, host, user, password, database;
	private Connection connect;
	private long timestampOfLastAction = Long.MIN_VALUE;
	private final Properties connectionProperties;

	public MySQLAdapter(String host, String user, String password, String database) {
		this("mysql", host, user, password, database, new Properties());
	}
	
	public MySQLAdapter(String driver, String host, String user, String password, String database, Properties connectionProperties) {
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
				System.out.println("Closing MySQL Connection");
				close();
			}
		}));
	}

	private void connect() throws SQLException {

		int tries = 0;
		do {
			try {
				Properties connectionProps = new Properties(connectionProperties);
				connectionProps.put("user", user);
				connectionProps.put("password", password);
				connect = DriverManager.getConnection("jdbc:" + driver + "://" + host + "/" + database, connectionProps);
				return;
			} catch (SQLException e) {
				tries ++;
				System.err.println("Connection to server " + host + " failed with JDBC driver " + driver + " (attempt " + tries + " of 3), waiting 3 seconds and trying again.");
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

	public synchronized void checkConnection() throws SQLException {
		int renewAfterSeconds = 5 * 60;
		if (timestampOfLastAction + renewAfterSeconds * 1000 < System.currentTimeMillis()) {
			System.out.println("Reconnect to database");
			close();
			connect();
		}
		timestampOfLastAction = System.currentTimeMillis();
	}

	public ResultSet getResultsOfQuery(String query) throws SQLException {
		return getResultsOfQuery(query, new ArrayList<>());
	}

	public ResultSet getResultsOfQuery(String query, String[] values) throws SQLException {
		return getResultsOfQuery(query, Arrays.asList(values));
	}

	public ResultSet getResultsOfQuery(String query, List<String> values) throws SQLException {
		checkConnection();
		PreparedStatement statement = connect.prepareStatement(query);
		for (int i = 1; i <= values.size(); i++) {
			statement.setString(i, values.get(i - 1));
		}
		return statement.executeQuery();
	}

	public int insert(String sql, String[] values) throws SQLException {
		return insert(sql, Arrays.asList(values));
	}

	public int insert(String sql, List<String> values) throws SQLException {
		checkConnection();
		PreparedStatement stmt = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		for (int i = 1; i <= values.size(); i++) {
			String val = values.get(i-1);
			if (NumberUtils.isCreatable(val)) {
				if (val.contains("."))
					stmt.setDouble(i, NumberUtils.toDouble(val));
				else
					stmt.setInt(i, NumberUtils.toInt(val));
			}
			else {
				stmt.setString(i, values.get(i - 1));
			}
		}
		stmt.executeUpdate();
		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}
	
	public int insert(String table, Map<String,String> map) throws SQLException {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		List<String> values = new ArrayList<>();
		for (String key : map.keySet()) {
			if (map.get(key) == null)
				continue;
			if (sb1.length() != 0) {
				sb1.append(", ");
				sb2.append(", ");
			}
			sb1.append(key);
			sb2.append("?");
			values.add(map.get(key));
		}
		
		String statement = "INSERT INTO " + table + " (" + sb1.toString() + ") VALUES (" + sb2.toString() + ")";
		return insert(statement, values);
	}

	public void update(String sql, String[] values) throws SQLException {
		update(sql, Arrays.asList(values));
	}

	public void update(String sql, List<String> values) throws SQLException {
		checkConnection();
		PreparedStatement stmt = connect.prepareStatement(sql);
		for (int i = 1; i <= values.size(); i++) {
			stmt.setString(i, values.get(i - 1));
		}
		stmt.executeUpdate();
	}
	
	public void update(String table, Map<String,String> updateValues, Map<String,String> conditions) throws SQLException {
		checkConnection();
		StringBuilder updateSB = new StringBuilder();
		List<String> values = new ArrayList<>();
		for (String key : updateValues.keySet()) {
			if (updateSB.length() > 0)
				updateSB.append(", ");
			updateSB.append(key + " = (?)");
			values.add(updateValues.get(key));
		}
		
		StringBuilder conditionSB = new StringBuilder();
		for (String key : conditions.keySet()) {
			if (conditionSB.length() > 0)
				conditionSB.append(" AND ");
			conditionSB.append(key + " = (?)");
			values.add(conditions.get(key));
		}
		
		String sql = "UPDATE " + table + " SET " + updateSB.toString() + " WHERE " + conditionSB.toString();
		PreparedStatement stmt = connect.prepareStatement(sql);
		for (int i = 1; i <= values.size(); i++) {
			stmt.setString(i, values.get(i - 1));
		}
		stmt.executeUpdate();
	}

	/**
	 * Close the connection. No more queries can be sent after having the access object closed
	 */
	public void close() {
		try {

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getDriver() {
		return driver;
	}
}