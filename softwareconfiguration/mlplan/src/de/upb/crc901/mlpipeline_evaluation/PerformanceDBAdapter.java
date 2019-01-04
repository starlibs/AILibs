package de.upb.crc901.mlpipeline_evaluation;

import java.io.Closeable;
import java.io.IOException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hasco.model.ComponentInstance;
import jaicore.basic.SQLAdapter;
import jaicore.ml.cache.ReproducibleInstances;

/**
 * Database adapter for performance data. Functionality to store and save
 * performance values in a database. json to reproduce the
 * {@link ReproducibleInstances} is saved as well as the solution that produced
 * the performance value.
 * 
 * @author jmhansel
 *
 */
public class PerformanceDBAdapter implements Closeable {
	/** Logger for controlled output. */
	private static final Logger logger = LoggerFactory.getLogger(PerformanceDBAdapter.class);

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
				logger.info("Creating table for evaluations");
				sqlAdapter.update("CREATE TABLE `" + this.performanceSampleTableName + "` (\r\n"
						+ " `evaluation_id` int(10) NOT NULL AUTO_INCREMENT,\r\n" + " `composition` json NOT NULL,\r\n"
						+ " `train_trajectory` json NOT NULL,\r\n" + " `test_trajectory` json NOT NULL,\r\n"
						+ " `loss_function` varchar(200) NOT NULL,\r\n" + " `score` double NOT NULL,\r\n"
						+ " `evaluation_time_ms` bigint NOT NULL,\r\n"
						+ "`evaluation_date` timestamp NULL DEFAULT NULL," + "`hash_value` char(64) NOT NULL,"
						+ " PRIMARY KEY (`evaluation_id`)\r\n"
						+ ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin", new ArrayList<>());
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
	 * @param composition           - Solution composition.
	 * @param reproducableInstances - Instances object that includes the trajectory,
	 *                              i.e. all operations that have been applied to
	 *                              the instances like loading, splitting etc.
	 * @param testData              - The reproducible instances of the test data
	 *                              used for this evaluation process
	 * @param className             - the java qualified class name of the loss
	 *                              function that was used
	 * @return opt - Optional that contains the score corresponding to the
	 *         composition and the reproducible instances or is empty if no suiting
	 *         entry is found in the database.
	 */
	public Optional<Double> exists(ComponentInstance composition, ReproducibleInstances reproducibleInstances,
			ReproducibleInstances testData, String className) {
		Optional<Double> opt = Optional.empty();
		ObjectMapper mapper = new ObjectMapper();
		try {
			String compositionString = mapper.writeValueAsString(composition);
			String trainTrajectoryString = mapper.writeValueAsString(reproducibleInstances.getInstructions());
			String testTrajectoryString = mapper.writeValueAsString(testData.getInstructions());
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(compositionString.getBytes());
			md.update(trainTrajectoryString.getBytes());
			md.update(testTrajectoryString.getBytes());
			md.update(className.getBytes());
			byte[] digest = md.digest();
			String hexHash = (new HexBinaryAdapter()).marshal(digest);
			ResultSet rs = sqlAdapter.getResultsOfQuery(
					"SELECT score FROM " + this.performanceSampleTableName + " WHERE hash_value = '" + hexHash + "'");
			while (rs.next()) {
				double score = rs.getDouble("score");
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
	 * @param composition           - Solution composition
	 * @param reproducableInstances - Instances object that includes the trajectory,
	 *                              i.e. all operations that have been applied to
	 *                              the instances like loading, splitting etc.
	 * @param testData              - The reproducible instances of the test data
	 *                              used for this evaluation process
	 * @param score                 - Score achieved by the composition on the
	 *                              reproducible instances
	 * @param className             - The java qualified class name of the loss
	 *                              function that was used
	 * @param evaluationTime        - The time it took for the corresponding
	 *                              evaluation in milliseconds
	 */
	public void store(ComponentInstance composition, ReproducibleInstances reproducibleInstances,
			ReproducibleInstances testData, double score, String className, long evaluationTime) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String compositionString = mapper.writeValueAsString(composition);
			String trainTrajectoryString = mapper.writeValueAsString(reproducibleInstances.getInstructions());
			String testTrajectoryString = mapper.writeValueAsString(testData.getInstructions());
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(compositionString.getBytes());
			md.update(trainTrajectoryString.getBytes());
			md.update(testTrajectoryString.getBytes());
			md.update(className.getBytes());
			byte[] digest = md.digest();
			String hexHash = (new HexBinaryAdapter()).marshal(digest);
			ResultSet rs = sqlAdapter.getResultsOfQuery(
					"SELECT score FROM " + this.performanceSampleTableName + " WHERE hash_value = '" + hexHash + "'");
			if (rs.next())
				return;
			Map<String, String> valueMap = new HashMap<>();
			valueMap.put("composition", compositionString);
			valueMap.put("train_trajectory", trainTrajectoryString);
			valueMap.put("test_trajectory", testTrajectoryString);
			valueMap.put("loss_function", className);
			valueMap.put("evaluation_date",
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date.from(Instant.now())));
			valueMap.put("evaluation_time_ms", Long.toString(evaluationTime));
			valueMap.put("hash_value", hexHash);
			valueMap.put("score", Double.toString(score));
			this.sqlAdapter.insert(this.performanceSampleTableName, valueMap);
		} catch (JsonProcessingException | NoSuchAlgorithmException | SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		this.sqlAdapter.close();
	}

}
