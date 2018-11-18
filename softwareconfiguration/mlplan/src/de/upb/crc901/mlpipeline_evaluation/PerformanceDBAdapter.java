package de.upb.crc901.mlpipeline_evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;

import hasco.model.ComponentInstance;
import hasco.serialization.CompositionSerializer;
import jaicore.basic.SQLAdapter;
import jaicore.ml.cache.ReproducibleInstances;

/**
 * Database adapter for performance data
 * 
 * @author jmhansel
 *
 */
public class PerformanceDBAdapter {

	private final SQLAdapter sqlAdapter;
	private final String performanceSampleTableName;

	public PerformanceDBAdapter(SQLAdapter sqlAdapter, String performanceSampleTableName) {
		this.sqlAdapter = sqlAdapter;
		this.performanceSampleTableName = performanceSampleTableName;

		/* initialize tables if not existent */
		try {
			ResultSet rs = sqlAdapter.getResultsOfQuery("SHOW TABLES");
			boolean hasPerformanceTable = false;
			while (rs.next()) {
				String tableName = rs.getString(1);
				if (tableName.equals(this.performanceSampleTableName))
					hasPerformanceTable = true;
			}

			// if there is no performance table, create it
			if (!hasPerformanceTable) {
				System.out.println("Creating table for evaluations");
				sqlAdapter.update(
						"CREATE TABLE `" + this.performanceSampleTableName + "` (\r\n" + " `evaluation_id` int(10) NOT NULL AUTO_INCREMENT,\r\n"
								+ " `composition` json NOT NULL,\r\n" + " `trajectory` json NOT NULL,\r\n"
								+ " `score` double NOT NULL,\r\n" + " PRIMARY KEY (`evaluation_id`)\r\n"
								+ ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin",
						new ArrayList<>());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Checks whether there is an entry for the composition and corresponding
	 * evaluation specified by the reproducable instances. If so, it returns the
	 * corresponding performance score.
	 * 
	 * @param composition
	 * @param reproducableInstances
	 * @return
	 */
	public Optional<Double> exists(ComponentInstance composition, ReproducibleInstances reproducableInstances) {
		double result = 0.0;
		Optional<Double> opt = Optional.of(result);
		return opt;
	}

	/**
	 * Stores the composition, the trajectory and the achieved score in the database.
	 * 
	 * @param composition
	 * @param reproducableInstances
	 * @param score
	 */
	public void store(ComponentInstance composition, ReproducibleInstances reproducableInstances, double score) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String compositionString = mapper.writeValueAsString(composition);
			String trajectoryString = mapper.writeValueAsString(reproducableInstances);
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
