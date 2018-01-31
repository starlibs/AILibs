package de.upb.crc901.mlplan.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.eventbus.Subscribe;

import de.upb.crc901.mlplan.search.evaluators.PipelineMeasurementEvent;
import jaicore.basic.MathExt;
import scala.compat.Platform;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.core.OptionHandler;

public class MySQLReductionExperimentLogger {
	private final Connection connect;
	private Statement statement = null;
	
	/**
	 * Initialize connection to database
	 * 
	 * @param host
	 * @param user
	 * @param password
	 * @param database
	 */
	public MySQLReductionExperimentLogger(String host, String user, String password, String database) {
		Connection initConnection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Properties connectionProps = new Properties();
			connectionProps.put("user", user);
			connectionProps.put("password", password);
			initConnection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, connectionProps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		connect = initConnection;
	}
	
	public ResultSet executeQuery(String query) throws SQLException {
		this.statement = connect.createStatement();
		return this.statement.executeQuery(query);
	}
	
	protected PreparedStatement getPreparedStatement(String sql) throws SQLException {
		return connect.prepareStatement(sql);
	}
	
	/**
	 * Close the connection. No more queries can be sent after having the access object closed
	 */
	public void close() {
		try {
			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addEvaluationEntry(String dataset, String left, String inner, String right, DescriptiveStatistics errorRates, DescriptiveStatistics runTimes) {
		
		PreparedStatement stmt = null;
		try {
			stmt = connect.prepareStatement("INSERT INTO `reductionstumps` (dataset, left_classifier, inner_classifier, right_classifier, n, error_rate_min, error_rate_max, error_rate_mean, error_rate_std, runtime_min, runtime_max, runtime_mean, runtime_std) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			int key = 0;
			stmt.setString(++key, dataset);
			stmt.setString(++key, left);
			stmt.setString(++key, inner);
			stmt.setString(++key, right);
			if (errorRates.getN() > 0) {
				stmt.setInt(++key, (int)errorRates.getN());
				stmt.setDouble(++key, MathExt.round(errorRates.getMin(), 4));
				stmt.setDouble(++key, MathExt.round(errorRates.getMax(), 4));
				stmt.setDouble(++key, MathExt.round(errorRates.getMean(), 4));
				stmt.setDouble(++key, MathExt.round(errorRates.getStandardDeviation(), 4));
				stmt.setInt(++key, (int)runTimes.getMin());
				stmt.setInt(++key, (int)runTimes.getMax());
				stmt.setInt(++key, (int)runTimes.getMean());
				stmt.setDouble(++key, runTimes.getStandardDeviation());
			}
			else {
				stmt.setInt(++key, 0);
				for (key++; key <= 13; key++)
					stmt.setNull(key, Types.NULL);
			}
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}