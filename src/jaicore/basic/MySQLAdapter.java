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
import java.util.Properties;

@SuppressWarnings("serial")
public class MySQLAdapter implements Serializable {
	private final String host, user, password, database;
	private Connection connect;
	private long timestampOfLastAction = Long.MIN_VALUE;

	public MySQLAdapter(String host, String user, String password, String database) {
		super();
		this.host = host;
		this.user = user;
		this.password = password;
		this.database = database;

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
				Properties connectionProps = new Properties();
				connectionProps.put("user", user);
				connectionProps.put("password", password);
				connect = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, connectionProps);
				return;
			} catch (SQLException e) {
				tries ++;
				System.err.println("Connection to server failed (attempt " + tries + " of 3), waiting 3 seconds and trying again.");
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

	private synchronized void checkConnection() throws SQLException {
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
			stmt.setString(i, values.get(i - 1));
		}
		stmt.executeUpdate();
		ResultSet rs = stmt.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
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
}