package ida2018;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.basic.MathExt;
import jaicore.basic.MySQLAdapter;

public class Table1Collector {
	public static void main(String[] args) throws SQLException {
		MySQLAdapter adapter = IDA2018Util.getAdapter();

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
				if (!single.containsKey(classifier))
					single.put(classifier, new HashMap<>());
				Map<String, Double> row = single.get(classifier);
				row.put(dataset, rs.getDouble("errorRate_mean"));
			}
		}

		/* collect data for homogeneous 1-step reduction runs */
		Map<String, Map<String, Double>> homogeneousStumps = new HashMap<>();
		{
			ResultSet rs =  adapter.getResultsOfQuery("SELECT * FROM RandomBestOfKHomogeneousStumpAveraged WHERE n >= " + minNumberOfResults);
			while (rs.next()) {
				String dataset = rs.getString("dataset");
				if (!datasets.contains(dataset))
					datasets.add(dataset);
				String classifier = rs.getString("classifier");
				if (!classifiers.contains(classifier))
					classifiers.add(classifier);
				if (!homogeneousStumps.containsKey(classifier))
					homogeneousStumps.put(classifier, new HashMap<>());
				Map<String, Double> row = homogeneousStumps.get(classifier);
				row.put(dataset, rs.getDouble("errorRate_mean"));
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
				if (!homogeneousEnsembles.containsKey(classifier))
					homogeneousEnsembles.put(classifier, new HashMap<>());
				Map<String, Double> row = homogeneousEnsembles.get(classifier);
				row.put(dataset, rs.getDouble("errorRate_mean"));
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
				if (!baggingResults.containsKey(classifier))
					baggingResults.put(classifier, new HashMap<>());
				Map<String, Double> row = baggingResults.get(classifier);
				row.put(dataset, rs.getDouble("errorRate_mean"));
			}
		}

		/* prepare latex table */
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin{tabular}{cc");
		for (int i = 0; i < datasets.size(); i++) {
			sb.append("r");
		}
		sb.append("c}\n &");
		for (String dataset : datasets) {
			sb.append("& \\rotatebox[origin=l]{90}{" + dataset.substring(0, dataset.lastIndexOf(".")) + "}");
		}
		sb.append("\\\\\\hline\n");
		Collections.sort(classifiers);
		for (String classifier : classifiers) {
			
			if (classifier.contains("VotedPerceptron") || classifier.contains("OneR") || classifier.contains("ZeroR"))
				continue;

			int stumpVsSingleWins = 0;
			int stumpVsSingleDraws = 0;
			int stumpVsSingleLosses = 0;
			
			int ensembleVsBaggingWins = 0;
			int ensembleVsBaggingDraws = 0;
			int ensembleVsBaggingLosses = 0;

			/* skip classifier if there are no results */
			boolean hasSingleClassifierValue = datasets.stream()
					.filter(d -> single.containsKey(classifier) && single.get(classifier).containsKey(d) && single.get(classifier).get(d) >= 0).findFirst().isPresent();
			boolean hasHomogeneousClassifierValue = datasets.stream()
					.filter(d -> homogeneousStumps.containsKey(classifier) && homogeneousStumps.get(classifier).containsKey(d) && homogeneousStumps.get(classifier).get(d) >= 0)
					.findFirst().isPresent();
			if (!hasSingleClassifierValue && !hasHomogeneousClassifierValue)
				continue;

			String displayedClassifierName = classifier.substring(classifier.lastIndexOf(".") + 1);
			switch (displayedClassifierName) {
			default:
				displayedClassifierName = displayedClassifierName.replaceAll("[a-z]", "");
				break;
			}
			sb.append("\\multirow{4}{*}{\\rotatebox[origin=c]{90}{" + displayedClassifierName + "}} & sa");
			Map<String, Double> saRow = single.containsKey(classifier) ? single.get(classifier) : new HashMap<>();
			Map<String, Double> oneSRRow = homogeneousStumps.containsKey(classifier) ? homogeneousStumps.get(classifier) : new HashMap<>();
			Map<String, Double> baggingRow = baggingResults.containsKey(classifier) ? baggingResults.get(classifier) : new HashMap<>();
			Map<String, Double> ensembleRow = homogeneousEnsembles.containsKey(classifier) ? homogeneousEnsembles.get(classifier) : new HashMap<>();
			StringBuilder row1 = new StringBuilder();
			StringBuilder row2 = new StringBuilder();
			StringBuilder row3 = new StringBuilder();
			StringBuilder row4 = new StringBuilder();
			for (String dataset : datasets) {
				double saVal = saRow.containsKey(dataset) ? saRow.get(dataset) : -1;
				double oneSRVal = oneSRRow.containsKey(dataset) ? oneSRRow.get(dataset) : -1;
				double baggingVal = baggingRow.containsKey(dataset) ? baggingRow.get(dataset) : -1;
				double ensembleVal = ensembleRow.containsKey(dataset) ? ensembleRow.get(dataset) : -1;
				
				/* compute battle results of single vs reduction stump */
				if (saVal < 0 || oneSRVal < 0);
//					stumpVsSingleDraws++;
				else {
					if (saVal > oneSRVal)
						stumpVsSingleWins++;
					else if (saVal < oneSRVal)
						stumpVsSingleLosses++;
					else
						stumpVsSingleDraws++;
				}
				
				/* compute battle results of single vs ensemble */
				if (baggingVal < 0 || ensembleVal < 0) {
//					ensembleVsSingleDraws ++;
				}
				else {
					if (baggingVal > ensembleVal)
						ensembleVsBaggingWins++;
					else if (baggingVal < ensembleVal)
						ensembleVsBaggingLosses++;
					else
						ensembleVsBaggingDraws++;
				}
				row1.append("&" + (saVal >= 0 ? MathExt.round(saVal * 100, 1) : "-"));
				row2.append("&" + (oneSRVal >= 0 ? MathExt.round(oneSRVal * 100, 1) : "-"));
				row3.append("&" + (baggingVal >= 0 ? MathExt.round(baggingVal * 100, 1) : "-"));
				row4.append("&" + (ensembleVal >= 0 ? MathExt.round(ensembleVal * 100, 1) : "-"));
			}
			sb.append(row1.toString());
			sb.append("&");
			sb.append("\\\\\n\t&1sr");
			sb.append(row2.toString());
			sb.append("&" + stumpVsSingleWins + "/" + stumpVsSingleDraws + "/" + stumpVsSingleLosses);
			sb.append("\\\\\n\t&ba");
			sb.append(row3.toString());
			sb.append("&");
			sb.append("\\\\\n&en");
			sb.append(row4.toString());
			sb.append("&" + ensembleVsBaggingWins + "/" + ensembleVsBaggingDraws + "/" + ensembleVsBaggingLosses);
			sb.append("\\\\\\hline\n");
		}
		sb.append("\\end{tabular}");
		System.out.println(sb.toString());
	}
}
