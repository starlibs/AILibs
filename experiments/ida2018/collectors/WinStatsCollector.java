package ida2018.collectors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.basic.MySQLAdapter;

public class WinStatsCollector {
	public static void main(String[] args) throws SQLException {
		MySQLAdapter adapter = new MySQLAdapter("isys-db.cs.upb.de", "ida2018", "WsFg33sE6aghabMr", "results_reduction");

		int minNumberOfResults = 3;
		List<String> datasets = new ArrayList<>();
		List<String> classifiers = new ArrayList<>();

		/* collect data for single classifiers */
		Map<String, Map<String, Double>> single = new HashMap<>();
		{
			ResultSet rs = adapter.getResultsOfQuery("SELECT * FROM ClassifierAveragePerformance WHERE n >= " + minNumberOfResults);
			while (rs.next()) {
				String dataset = rs.getString("dataset");
				String classifier = rs.getString("classifier");
				if (!classifiers.contains(classifier))
					classifiers.add(classifier);
				if (!single.containsKey(dataset))
					single.put(dataset, new HashMap<>());
				Map<String, Double> row = single.get(dataset);
				row.put(classifier, rs.getDouble("errorRate_mean"));
			}
		}

		/* collect data for homogeneous 1-step reduction runs */
		Map<String, Map<String, Double>> homogeneousStumps = new HashMap<>();
		{
			ResultSet rs = adapter.getResultsOfQuery("SELECT * FROM RandomBestOfKHomogeneousStumpAveraged WHERE n >= " + minNumberOfResults);
			while (rs.next()) {
				String dataset = rs.getString("dataset");
				if (!datasets.contains(dataset))
					datasets.add(dataset);
				String classifier = rs.getString("classifier");
				if (!classifiers.contains(classifier))
					classifiers.add(classifier);
				if (!homogeneousStumps.containsKey(dataset))
					homogeneousStumps.put(dataset, new HashMap<>());
				Map<String, Double> row = homogeneousStumps.get(dataset);
				row.put(classifier, rs.getDouble("errorRate_mean"));
			}
		}

		/* collect data for homogeneous 1-step reduction runs */
		Map<String, Map<String, Double>> heterogeneousStumps = new HashMap<>();
		{
			ResultSet rs = adapter.getResultsOfQuery("SELECT * FROM RandomBestOfKHeterogeneousStumpAveragePerformance WHERE n >= " + minNumberOfResults);
			while (rs.next()) {
				String dataset = rs.getString("dataset");
				if (!datasets.contains(dataset))
					datasets.add(dataset);
				String classifier = rs.getString("left_classifier") + "/" + rs.getString("inner_classifier") + "/" + rs.getString("right_classifier");
				if (!classifiers.contains(classifier))
					classifiers.add(classifier);
				if (!heterogeneousStumps.containsKey(dataset))
					heterogeneousStumps.put(dataset, new HashMap<>());
				Map<String, Double> row = heterogeneousStumps.get(dataset);
				row.put(classifier, rs.getDouble("errorRate_mean"));
			}
		}

		/* collect data for homogeneous ensembles */
		Map<String, Map<String, Double>> homogeneousEnsembles = new HashMap<>();
		{
			ResultSet rs = adapter.getResultsOfQuery("SELECT * FROM EnsembleAveragePerformance WHERE n >= " + minNumberOfResults);
			while (rs.next()) {
				String dataset = rs.getString("dataset");
				if (!datasets.contains(dataset))
					datasets.add(dataset);
				String classifier = rs.getString("classifier");
				if (!classifiers.contains(classifier))
					classifiers.add(classifier);
				if (!homogeneousEnsembles.containsKey(dataset))
					homogeneousEnsembles.put(dataset, new HashMap<>());
				Map<String, Double> row = homogeneousEnsembles.get(dataset);
				row.put(classifier, rs.getDouble("errorRate_mean"));
			}
		}

		/* collect data for bagging */
		Map<String, Map<String, Double>> baggingResults = new HashMap<>();
		{
			ResultSet rs = adapter.getResultsOfQuery("SELECT * FROM BaggingAveragePerformance WHERE n >= " + minNumberOfResults);
			while (rs.next()) {
				String dataset = rs.getString("dataset");
				if (!datasets.contains(dataset))
					datasets.add(dataset);
				String classifier = rs.getString("classifier");
				if (!classifiers.contains(classifier))
					classifiers.add(classifier);
				if (!baggingResults.containsKey(dataset))
					baggingResults.put(dataset, new HashMap<>());
				Map<String, Double> row = baggingResults.get(dataset);
				row.put(classifier, rs.getDouble("errorRate_mean"));
			}
		}

		/* for each dataset, check the best algorithm */
		Map<String, Integer> wins = new HashMap<>();
		for (String dataset : datasets) {

			System.out.println(dataset);
			String bestAlgo = null;
			double bestOverallPerformance = Double.MAX_VALUE;

			/* compute best individual classifier */
			double bestSingleResult = Double.MAX_VALUE;
			String bestSingleClassifier = null;
			if (single.containsKey(dataset)) {
				for (String classifier : single.get(dataset).keySet()) {
					double val = single.get(dataset).get(classifier);
					if (val >= 0 && val < bestSingleResult) {
						bestSingleResult = val;
						bestSingleClassifier = classifier;
					}
				}
			}
			System.out.println("\tIndividual:    " + bestSingleResult + " (" + bestSingleClassifier + ")");
			if (bestSingleResult < bestOverallPerformance) {
				bestOverallPerformance = bestSingleResult;
				bestAlgo = "individual";
			}

			/* compute best homogeneous */
			double bestHomogeneousResult = Double.MAX_VALUE;
			String bestHomogeneousSingleClassifier = null;
			if (homogeneousStumps.containsKey(dataset)) {
				for (String classifier : homogeneousStumps.get(dataset).keySet()) {
					double val = homogeneousStumps.get(dataset).get(classifier);
					if (val < bestHomogeneousResult) {
						bestHomogeneousResult = val;
						bestHomogeneousSingleClassifier = classifier;
					}
				}
			}
			System.out.println("\tHomogeneous:   " + bestHomogeneousResult + " (" + bestHomogeneousSingleClassifier + ")");
			if (bestHomogeneousResult < bestOverallPerformance) {
				bestOverallPerformance = bestHomogeneousResult;
				bestAlgo = "homogeneous";
			}

			/* compute best heterogeneous */
			double bestHeterogeneousResult = Double.MAX_VALUE;
			String bestHeterogeneousSingleClassifier = null;
			if (heterogeneousStumps.containsKey(dataset)) {
				for (String classifier : heterogeneousStumps.get(dataset).keySet()) {
					double val = heterogeneousStumps.get(dataset).get(classifier);
					if (val < bestHeterogeneousResult) {
						bestHeterogeneousResult = val;
						bestHeterogeneousSingleClassifier = classifier;
					}
				}
			}
			System.out.println("\tHeterogeneous: " + bestHeterogeneousResult + " (" + bestHeterogeneousSingleClassifier + ")");
			if (bestHeterogeneousResult < bestOverallPerformance) {
				bestOverallPerformance = bestHeterogeneousResult;
				bestAlgo = "heterogeneous";
			}

			/* compute best Bagging */
			double bestBaggingResult = Double.MAX_VALUE;
			String bestBaggingClassifier = null;
			if (baggingResults.containsKey(dataset)) {
				for (String classifier : baggingResults.get(dataset).keySet()) {
					double val = baggingResults.get(dataset).get(classifier);
					if (val >= 0 && val < bestBaggingResult) {
						bestBaggingResult = val;
						bestBaggingClassifier = classifier;
					}
				}
			}
			System.out.println("\tBagging:       " + bestBaggingResult + " (" + bestBaggingClassifier + ")");
			if (bestBaggingResult < bestOverallPerformance) {
				bestOverallPerformance = bestBaggingResult;
				bestAlgo = "bagging";
			}

			/* compute best ensemble */
			double bestEnsembleResult = Double.MAX_VALUE;
			String bestEnsembleClassifier = null;
			if (homogeneousEnsembles.containsKey(dataset)) {
				for (String classifier : homogeneousEnsembles.get(dataset).keySet()) {
					double val = homogeneousEnsembles.get(dataset).get(classifier);
					if (val >= 0 && val < bestEnsembleResult) {
						bestEnsembleResult = val;
						bestEnsembleClassifier = classifier;
					}
				}
			}
			System.out.println("\tHomogen. Ensm: " + bestEnsembleResult + " (" + bestEnsembleClassifier + ")");
			if (bestEnsembleResult < bestOverallPerformance) {
				bestOverallPerformance = bestEnsembleResult;
				bestAlgo = "ensemble";
			}

			System.out.println("\tWinner: " + bestAlgo);
			if (!wins.containsKey(bestAlgo))
					wins.put(bestAlgo, 0);
			wins.put(bestAlgo, wins.get(bestAlgo) + 1);
		}
		System.out.println("Scores: ");
		wins.keySet().forEach(k -> System.out.println("\t" + k + ": " + wins.get(k)));
	}
}
