package autofe.experiments.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class ResultTableCollector {

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
			List<Double> testErrorRates = t.getValueAsDoubleList("kendallsTau", ",");
			for (int i = 0; i < testErrorRates.size(); i++)
				testErrorRates.set(i, Math.abs(testErrorRates.get(i)));

			if (testErrorRates.size() < 2) {
				testErrorRates.add(testErrorRates.get(0));
			}
			t.store("kendallsTau", ListHelper.implode(testErrorRates, ","));
			t.store("kendallsTau_mean", StatisticsUtil.mean(t.getValueAsDoubleList("kendallsTau", ",")));
			t.store("kendallsTau_stddev", StatisticsUtil.standardDeviation(t.getValueAsDoubleList("kendallsTau", ",")));

			if (t.getValueAsString("dataset").contains(".")) {
				t.store("dataset",
						t.getValueAsString("dataset").substring(0, t.getValueAsString("dataset").lastIndexOf(".")));
			}

			if (!datasets.contains(t.getValueAsString("dataset"))) {
				datasets.add(t.getValueAsString("dataset"));
			}

			// t.store("dataset", "\\multicolumn{1}{c}{\\rotatebox[origin=l]{90}{" +
			// t.getValueAsString("dataset") + "}}");
			t.store("dataset", "\\multicolumn{1}{l}{" + t.getValueAsString("dataset") + "}");
		}

		Map<String, IKVFilter> filterConfig = new HashMap<>();
		filterConfig.put("kendallsTau", inverseFilter);
		filterConfig.put("kendallsTau_mean", inverseFilter);
		csvChunks.applyFilter(filterConfig);
		csvChunks.tTest("dataset", "benchmark", "kendallsTau", "Random", "ttest");
		csvChunks.best("dataset", "benchmark", "kendallsTau_mean", "best");
		csvChunks.sort(new TaskKeyComparator(new String[] { "benchmark", "dataset" }));
		csvChunks.applyFilter(filterConfig);

		for (Task t : csvChunks) {
			t.store("entry",
					ValueUtil.valueToString(t.getValueAsDouble("kendallsTau_mean"), 2) + " \\begin{small}($\\pm$ "
							+ ValueUtil.valueToString(t.getValueAsDouble("kendallsTau_stddev"), 2) + ")\\end{small}");

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
