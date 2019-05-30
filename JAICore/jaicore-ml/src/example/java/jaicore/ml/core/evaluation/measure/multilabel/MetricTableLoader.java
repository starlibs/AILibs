package jaicore.ml.core.evaluation.measure.multilabel;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;
import jaicore.ml.core.evaluation.measure.ClassifierMetricGetter;

/**
* This is a helper class to setup the metric table.
*
* @author mwever
*
*/
public class MetricTableLoader {

	private static final Logger logger = LoggerFactory.getLogger(MetricTableLoader.class);

	public static void main(final String[] args) {
		SQLAdapter adapter = new SQLAdapter(args[0], args[1], args[2], args[3], Boolean.parseBoolean(args[4]));

		for (String multiLabelMetric : ClassifierMetricGetter.getMultiLabelMetrics()) {
			Map<String, String> metricDescription = new HashMap<>();
			metricDescription.put("metric_name", multiLabelMetric);
			metricDescription.put("version", "1");
			metricDescription.put("classifier_type", "multilabel");
			try {
				adapter.insert("metric", metricDescription);
			} catch (SQLException e) {
				logger.error("Could not insert metric into metric table.", e);
			}
		}

		for (String multiLabelMetric : ClassifierMetricGetter.getSingleLabelMetrics()) {
			Map<String, String> metricDescription = new HashMap<>();
			metricDescription.put("metric_name", multiLabelMetric);
			metricDescription.put("version", "1");
			metricDescription.put("classifier_type", "singlelabel");
			try {
				adapter.insert("metric", metricDescription);
			} catch (SQLException e) {
				logger.error("Could not insert metric into metric table.", e);
			}
		}

		adapter.close();
	}

}