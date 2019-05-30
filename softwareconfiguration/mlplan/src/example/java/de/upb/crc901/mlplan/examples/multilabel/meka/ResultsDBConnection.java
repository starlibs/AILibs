package de.upb.crc901.mlplan.examples.multilabel.meka;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;

/**
* Connection for uploading results of AutoMLC experiments.
*
* @author Helena Graf
*
*/
public class ResultsDBConnection {

	/**
	 * Logger for controllable outputs.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResultsDBConnection.class);

	private static final String LABEL_METRIC_ID = "metric_id";

	/**
	 * The adapter to access the SQL database.
	 */
	private final SQLAdapter adapter;

	/**
	 * Id for the run this connection is used in.
	 */
	private final int experimentId;

	private final String intermediateMeasurementsTableName;

	private final String finalMeasurementsTableName;

	private final String algorithmName;

	/**
	 * Table which contains all the metric ids.
	 */
	private final String metricIdsTableName;

	public ResultsDBConnection(final String intermediateMeasurementsTableName, final String finalMeasurementsTableName, final String metricIdsTableName, final int runId, final String algorithmName, final SQLAdapter adapter) {
		this.finalMeasurementsTableName = finalMeasurementsTableName;
		this.metricIdsTableName = metricIdsTableName;
		this.intermediateMeasurementsTableName = intermediateMeasurementsTableName;
		this.experimentId = runId;
		this.algorithmName = algorithmName;
		this.adapter = adapter;
	}

	public void addFinalMeasurements(final Map<String, Double> metricsWithValues) throws SQLException {
		LOGGER.debug("Add final measurements to db");
		for (Map.Entry<String, Double> entry : metricsWithValues.entrySet()) {
			if (entry.getValue().isNaN()) {
				continue;
			}

			HashMap<String, Object> map = new HashMap<>();
			map.put("experiment_id", this.experimentId);
			map.put("found_by", this.algorithmName);
			map.put(LABEL_METRIC_ID, this.getLatestIdForMetric(entry.getKey()));
			map.put("value", entry.getValue());
			this.adapter.insertNoNewValues(this.finalMeasurementsTableName, map);
		}
		LOGGER.debug("Done adding final measurements to db.");
	}

	public void addIntermediateMeasurement(final String classifier, final double value, final long foundAt, final int generation) throws SQLException {
		LOGGER.debug("Add intermediate measurement to db");
		HashMap<String, Object> map = new HashMap<>();
		map.put("experiment_id", this.experimentId);
		map.put("found_by", this.algorithmName);
		map.put("found_at", foundAt);
		map.put("generation", generation);
		map.put("classifier_string", classifier);
		map.put("value", value);
		this.adapter.insertNoNewValues(this.intermediateMeasurementsTableName, map);
		LOGGER.debug("Done adding intermediate measurement to db");
	}

	/**
	 * Gets the id of the latest version of the metric with the given name from the
	 * database.
	 *
	 * @param metricName
	 *            The metric for which to get the id
	 * @return The id of the latest version of the metric, null if not present
	 * @throws SQLException
	 *             If something goes wrong while connecting to the db
	 */
	public Integer getLatestIdForMetric(final String metricName) throws SQLException {
		// Get metric
		String query = String.format("SELECT %s FROM %s WHERE %s=? ORDER BY %s DESC", LABEL_METRIC_ID, this.metricIdsTableName, "metric_name", "updated_at");
		List<String> values = Arrays.asList(metricName);
		ResultSet resultSet = this.adapter.getResultsOfQuery(query, values);

		// Return latest
		if (resultSet.next()) {
			return resultSet.getInt(LABEL_METRIC_ID);
		} else {
			LOGGER.warn("Requested metric with name {} which is not present in the db.", metricName);
			return null;
		}
	}
}