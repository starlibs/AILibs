package autofe.db.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.Database;

public class DatabaseHelper {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

	private Connection con;

	private Database db;

	public DatabaseHelper(Database db) {
		super();
		this.db = db;
		try {
			initConnection();
		} catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException("Cannot establish JDBC connection", e);
		}
	}

	public void executeSql(String sql) {
		LOG.debug("Executing SQL: {}", sql);
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			String err = String.format("Cannot execute SQL: %s", sql);
			throw new RuntimeException(err, e);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public ResultSet executeSelect(String sql) {
		LOG.debug("Executing select: {}", sql);
		Statement stmt = null;
		ResultSet toReturn = null;
		try {
			stmt = con.createStatement();
			stmt.execute(sql);
			toReturn = stmt.getResultSet();
		} catch (SQLException e) {
			String err = String.format("Cannot execute SQL: %s", sql);
			throw new RuntimeException(err, e);
		}
		return toReturn;
	}

	public void close(ResultSet rs) {
		try {
			rs.getStatement().close();
		} catch (SQLException e) {
			LOG.warn("Cannot close statement", e);
		}
	}

	public void commit() {
		LOG.debug("Do commit..");
		try {
			con.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot commit!", e);
		}
	}

	public void rollback() {
		LOG.debug("Do rollback..");
		try {
			con.rollback();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot rollback!", e);
		}
	}

	public void initConnection() throws ClassNotFoundException, SQLException {
		// Class.forName(db.getJdbcDriver());
		con = DriverManager.getConnection(db.getJdbcUrl(), db.getJdbcUsername(), db.getJdbcPassword());
		con.setAutoCommit(false);
	}
	
	public void closeConnection() {
		try {
			con.close();
		} catch (SQLException e) {
			LOG.error("Cannot close connection");
		}
	}

}
