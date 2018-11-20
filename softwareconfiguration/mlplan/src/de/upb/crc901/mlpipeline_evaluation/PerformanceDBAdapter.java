package de.upb.crc901.mlpipeline_evaluation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.model.ComponentInstance;
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

			// if there is no performance table, create it. we hash the composition and
			// trajectory and use the hash value as primary key for performance reasons.
			if (!hasPerformanceTable) {
				System.out.println("Creating table for evaluations");
				sqlAdapter.update(
						"CREATE TABLE `" + this.performanceSampleTableName + "` (\r\n"
								+ " `evaluation_id` int(10) NOT NULL AUTO_INCREMENT,\r\n"
								+ " `composition` json NOT NULL,\r\n" + " `trajectory` json NOT NULL,\r\n"
								+ " `score` double NOT NULL,\r\n" + "`evaluation_date` timestamp NULL DEFAULT NULL,"
								+ "`hash_value` CHAR(64) NOT NULL," + " PRIMARY KEY (`evaluation_id`)\r\n"
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
	 * 
	 * @param composition
	 *            - Solution composition.
	 * @param reproducableInstances
	 *            - Instances object that includes the trajectory, i.e. all
	 *            operations that have been applied to the instances like loading,
	 *            splitting etc.
	 * @return opt - Optional that contains the score corresponding to the
	 *         composition and the reproducible instances or is empty if no suiting
	 *         entry is found in the database.
	 */
	public Optional<Double> exists(ComponentInstance composition, ReproducibleInstances reproducibleInstances) {
		Optional<Double> opt = Optional.empty();
		ObjectMapper mapper = new ObjectMapper();
		try {
			String compositionString = mapper.writeValueAsString(composition);
			String trajectoryString = mapper.writeValueAsString(reproducibleInstances.getInstructions());

			// hash stuff for faster lookup
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(compositionString.getBytes());
			md.update(trajectoryString.getBytes());
			byte[] digest = md.digest();
			String hexHash = (new HexBinaryAdapter()).marshal(digest);
			ResultSet rs = sqlAdapter.getResultsOfQuery("SELECT score, composition, trajectory, hash_value FROM " + this.performanceSampleTableName
							+ " WHERE hash_value = '" + hexHash + "'");
			while (rs.next()) {
			String dbCompositionString = rs.getString("composition");
				String dbTrajectoryString = rs.getString("trajectory");
				double score = rs.getDouble("score");
				if (compositionString.equals(dbCompositionString) && trajectoryString.equals(dbTrajectoryString))
					opt = Optional.of(score);
			}
		} catch (JsonProcessingException | SQLException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return opt;
	}

	/**
	 * Stores the composition, the trajectory and the achieved score in the
	 * database.
	 * 
	 * @param composition
	 *            - Solution composition
	 * @param reproducableInstances
	 *            - Instances object that includes the trajectory, i.e. all
	 *            operations that have been applied to the instances like loading,
	 *            splitting etc.
	 * @param score
	 *            - Score achieved by the composition on the reproducible instances
	 */
	public void store(ComponentInstance composition, ReproducibleInstances reproducibleInstances, double score) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String compositionString = mapper.writeValueAsString(composition);
			String trajectoryString = mapper.writeValueAsString(reproducibleInstances.getInstructions());
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(compositionString.getBytes());
			// md.update(trajectoryString.getBytes());
			byte[] digest = md.digest();
			String hexHash = (new HexBinaryAdapter()).marshal(digest);
			Map<String, String> valueMap = new HashMap<>();
			valueMap.put("composition", compositionString);
			valueMap.put("trajectory", trajectoryString);
			valueMap.put("evaluation_date",
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(Instant.now())));
			valueMap.put("hash_value", hexHash);
			// TOOD check if this works
			valueMap.put("score", Double.toString(score));
			this.sqlAdapter.insert(this.performanceSampleTableName, valueMap);
		} catch (JsonProcessingException | NoSuchAlgorithmException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
