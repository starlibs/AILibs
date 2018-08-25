package autofe.experiments.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jaicore.basic.ListHelper;
import jaicore.basic.SQLAdapter;
import jaicore.basic.StatisticsUtil;
import jaicore.basic.ValueUtil;
import jaicore.basic.chunks.Task;
import jaicore.basic.chunks.TaskChunk;
import jaicore.basic.chunks.TaskChunkUtil;
import jaicore.basic.chunks.TaskKeyComparator;
import jaicore.basic.kvstore.IKVFilter;
import jaicore.basic.kvstore.KVStoreUtil;

public class Top3ResultTableCollector {

	private static final List<String> benchmarkFunctions = Arrays.asList("Random", "KernelCluster", "KernelLDA", // "Cluster",
																													// "LDA",
			"COCO", "COED");

	public static void main(final String[] args) throws Exception {

		IKVFilter inverseFilter = new IKVFilter() {
			@Override
			public String filter(String value) {
				String[] values = value.split(",");
				String result = "";
				for (int i = 0; i < values.length; i++) {

					result += new Double((-1) * Double.parseDouble(values[i])).toString();
					if (i != values.length - 1)
						result += ",";
				}
				return result;
			}
		};

		Map<String, String> commonFields = new HashMap<>();

		TaskChunk<Task> csvChunks = new TaskChunk<>("chunkID=baselines");

		TaskChunk<Task> cMLPlan = null;
		{
			try (SQLAdapter adapter = new SQLAdapter("localhost", "experiments2", "experiments123!", "experiments")) {
				cMLPlan = TaskChunkUtil.readFromMySQLTable(adapter, "benchmarkranking", commonFields);

				for (String benchmark : benchmarkFunctions) {
					Map<String, String> testMap = new HashMap<>();
					testMap.put("benchmark", benchmark);
					TaskChunk<Task> tmpTaskChunk = cMLPlan.select(testMap);

					tmpTaskChunk.projectRemove(new String[] { "exception" });

					tmpTaskChunk = tmpTaskChunk.group(new String[] { "dataset", "benchmark" }, new HashMap<>());
					csvChunks.addAll(tmpTaskChunk);
				}

			}
		}

		List<String> datasets = new LinkedList<>();

		/* Collect all the datasets for which we already have results */
		Set<String> datasetsForMLPlan = new HashSet<>();
		for (Task t : cMLPlan) {
			datasetsForMLPlan.add(t.getValueAsString("dataset"));
		}

		for (Task t : csvChunks) {
			List<Double[]> benchmarkRanking = Stream.of(t.getKeyValueMap().get("benchmarkRanking").split("\\],\\["))
					.map(x -> {
						String[] values = x.split(",");
						Double[] result = new Double[values.length];
						for (int i = 0; i < values.length; i++) {
							if (values[i].startsWith("[")) {
								result[i] = Double.parseDouble(values[i].substring(1));
							} else if (values[i].endsWith("]")) {
								result[i] = Double.parseDouble(values[i].substring(0, values[i].length() - 1));
							} else {
								result[i] = Double.parseDouble(values[i]);
							}
						}
						return result;
					}).collect(Collectors.toList());
			List<Double[]> mlplanRanking = Stream.of(t.getKeyValueMap().get("mlplanRanking").split("\\],\\["))
					.map(x -> {
						String[] values = x.split(",");
						Double[] result = new Double[values.length];
						for (int i = 0; i < values.length; i++) {
							if (values[i].startsWith("[")) {
								result[i] = Double.parseDouble(values[i].substring(1));
							} else if (values[i].endsWith("]")) {
								result[i] = Double.parseDouble(values[i].substring(0, values[i].length() - 1));
							} else {
								result[i] = Double.parseDouble(values[i]);
							}
						}
						return result;
					}).collect(Collectors.toList());

			List<Double> accs = new ArrayList<>();

			for (int i = 0; i < benchmarkRanking.size(); i++) {
				Map<Integer, Double> mlplanRank = new HashMap<>();
				Map<Integer, Double> benchmarkRank = new HashMap<>();

				for (int j = 0; j < mlplanRanking.size(); j++) {
					mlplanRank.put(j, mlplanRanking.get(i)[j]);
				}
				for (int j = 0; j < benchmarkRanking.size(); j++) {
					benchmarkRank.put(j, benchmarkRanking.get(i)[j]);
				}
				List<Entry<Integer, Double>> mlplanRankList = new ArrayList<>(mlplanRank.entrySet());
				mlplanRankList.sort(Entry.comparingByValue());
				List<Entry<Integer, Double>> benchmarkRankList = new ArrayList<>(benchmarkRank.entrySet());
				benchmarkRankList.sort(Entry.comparingByValue());

				int right = 0;
				for (int j = 0; j < 3; j++) {
					int key = mlplanRankList.get(j).getKey();
					boolean found = false;
					for (int k = 0; k < 3; k++) {
						if (benchmarkRankList.get(k).getKey() == key) {
							found = true;
							break;
						}

					}
					if (found)
						right++;
				}
				accs.add(((double) right) / 3d);
			}

			// List<Double> testErrorRates = t.getValueAsDoubleList("kendallsTau", ",");
			for (int i = 0; i < accs.size(); i++)
				accs.set(i, Math.abs(accs.get(i)));

			if (accs.size() < 2) {
				accs.add(accs.get(0));
			}
			t.store("top3acc", ListHelper.implode(accs, ","));
			t.store("top3acc_mean", StatisticsUtil.mean(accs));
			t.store("top3acc_stddev", StatisticsUtil.standardDeviation(accs));

			if (t.getValueAsString("dataset").contains(".")) {
				t.store("dataset",
						t.getValueAsString("dataset").substring(0, t.getValueAsString("dataset").lastIndexOf(".")));
			}

			// if (t.getValueAsString("dataset").startsWith("mnistr")) {
			// t.store("dataset", "mnistrot");
			// }
			if (!datasets.contains(t.getValueAsString("dataset"))) {
				datasets.add(t.getValueAsString("dataset"));
			}

			// t.store("dataset", "\\multicolumn{1}{c}{\\rotatebox[origin=l]{90}{" +
			// t.getValueAsString("dataset") + "}}");
			t.store("dataset", "\\multicolumn{1}{l}{" + t.getValueAsString("dataset") + "}");
		}

		Map<String, IKVFilter> filterConfig = new HashMap<>();
		filterConfig.put("top3acc", inverseFilter);
		filterConfig.put("top3acc_mean", inverseFilter);
		csvChunks.applyFilter(filterConfig);
		csvChunks.tTest("dataset", "benchmark", "top3acc", "Random", "ttest");
		csvChunks.best("dataset", "benchmark", "top3acc_mean", "best");
		csvChunks.sort(new TaskKeyComparator(new String[] { "benchmark", "dataset" }));
		csvChunks.applyFilter(filterConfig);

		for (

		Task t : csvChunks) {
			t.store("entry", ValueUtil.valueToString(t.getValueAsDouble("top3acc_mean"), 2) + " \\begin{small}($\\pm$ "
					+ ValueUtil.valueToString(t.getValueAsDouble("top3acc_stddev"), 2) + ")\\end{small}");

			if (t.getValueAsBoolean("best")) {
				t.store("entry", "\\textbf{" + t.getValueAsString("entry") + "}");
			}

			if (t.containsKey("ttest")) {
				switch (t.getValueAsString("ttest")) {
				case "impr":
					t.store("entry", t.getValueAsString("entry") + " $\\bullet$");
					break;
				case "deg":
					t.store("entry", t.getValueAsString("entry") + " $\\circ$");
					break;
				default:
					t.store("entry", t.getValueAsString("entry") + " $\\phantom{\\bullet}$");
					break;

				}
			} else {
				t.store("entry", t.getValueAsString("entry") + " $\\phantom{\\bullet}$");
			}
		}

		Collections.sort(datasets);

		for (TaskChunk<Task> c : new TaskChunk[] { csvChunks }) {
			String latexTable = KVStoreUtil.kvStoreCollectionToLaTeXTable(c.toKVStoreCollection(), "benchmark",
					"dataset", "entry", "-$\\phantom{\\bullet}$");
			System.out.println(latexTable);
		}

	}

}
