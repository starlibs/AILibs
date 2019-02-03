package jaicore.ml.evaluation.multilabel.databaseconnection;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jaicore.basic.SQLAdapter;
import jaicore.ml.evaluation.multilabel.ClassifierMetricGetter;

/**
 * This is a helper class to setup the metric table.
 *
 * @author wever
 *
 */
public class MetricTableLoader {

	public static void main(final String[] args) {
		SQLAdapter adapter = new SQLAdapter(args[0], args[1], args[2], args[3], Boolean.parseBoolean(args[4]));

		for (String multiLabelMetric : ClassifierMetricGetter.multiLabelMetrics) {
			Map<String, String> metricDescription = new HashMap<>();
			metricDescription.put("metric_name", multiLabelMetric);
			metricDescription.put("version", "1");
			metricDescription.put("classifier_type", "multilabel");
			try {
				adapter.insert("metric", metricDescription);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		for (String multiLabelMetric : ClassifierMetricGetter.singleLabelMetrics) {
			Map<String, String> metricDescription = new HashMap<>();
			metricDescription.put("metric_name", multiLabelMetric);
			metricDescription.put("version", "1");
			metricDescription.put("classifier_type", "singlelabel");
			try {
				adapter.insert("metric", metricDescription);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		adapter.close();
	}

}
