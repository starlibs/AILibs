package de.upb.crc901.mlplan.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import jaicore.basic.MathExt;
import jaicore.basic.MySQLAdapter;

public class MySQLReductionExperimentLogger extends MySQLAdapter {

	public MySQLReductionExperimentLogger (String host, String user, String password, String database) {
		super (host,user,password,database);
	}
	
	public void addEvaluationEntry(String dataset, String left, String inner, String right, DescriptiveStatistics errorRates, DescriptiveStatistics runTimes) {
		
		PreparedStatement stmt = null;
		try {
			stmt = getConnect().prepareStatement("INSERT INTO `reductionstumps` (dataset, left_classifier, inner_classifier, right_classifier, n, error_rate_min, error_rate_max, error_rate_mean, error_rate_std, runtime_min, runtime_max, runtime_mean, runtime_std) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
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