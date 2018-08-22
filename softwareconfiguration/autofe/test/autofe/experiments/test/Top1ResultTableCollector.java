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
import jaicore.basic.kvstore.KVStoreUtil;

public class Top1ResultTableCollector {

	private static final List<String> benchmarkFunctions = Arrays.asList("Random", "KernelCluster", "LDA", "KernelLDA", // "Cluster",
			"COCO", "COED");

	public static void main(final String[] args) throws Exception {
		Map<String, String> commonFields = new HashMap<>();

		TaskChunk<Task> csvChunks = new TaskChunk<>("chunkID=baselines");
		// csvChunks.addAll(cMLPlan);

		TaskChunk<Task> cMLPlan = null;
		{
			// try (SQLAdapter adapter = new SQLAdapter("localhost", "experiments2",
			// "experiments123!", "experiments")) {
			try (SQLAdapter adapter = new SQLAdapter("localhost", "experiments2", "experiments123!", "experiments")) {
				// commonFields.put("candidate", C_ML_PLAN);
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

			// System.out.println(t.getKeyValueMap().get("kendallsTau"));
			// System.out.println("----");
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

			System.out.println(Arrays.toString(benchmarkRanking.get(0)));
			System.out.println(Arrays.toString(mlplanRanking.get(0)));
			System.out.println("---");

			List<Double> diff = new ArrayList<>();

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

				Entry<Integer, Double> firstBenchmark = benchmarkRankList.get(0);
				diff.add(mlplanRankList.get(0).getValue() - mlplanRanking.get(i)[firstBenchmark.getKey()]);
			}

			// List<Double> testErrorRates = t.getValueAsDoubleList("kendallsTau", ",");
			for (int i = 0; i < diff.size(); i++)
				diff.set(i, Math.abs(diff.get(i)));

			if (diff.size() < 2) {
				diff.add(diff.get(0));
			}
			t.store("top1diff", ListHelper.implode(diff, ","));
			t.store("top1diff_mean", StatisticsUtil.mean(diff));

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

		csvChunks.tTest("dataset", "benchmark", "top1diff", "Random", "ttest");
		csvChunks.best("dataset", "benchmark", "top1diff_mean", "best");
		csvChunks.sort(new TaskKeyComparator(new String[] { "benchmark", "dataset" }));

		for (

		Task t : csvChunks) {
			t.store("entry", ValueUtil.valueToString(t.getValueAsDouble("top1diff_mean"), 2));

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
