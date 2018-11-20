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
import jaicore.basic.kvstore.KVStoreUtil;

public class OneTwoPhaseResultTableCollector {
	private static final List<String> ALGORITHMS = Arrays.asList("none", "cluster", "lda", "coco", "coed");

	public static void main(String[] args) throws Exception {
		Map<String, String> commonFields = new HashMap<>();

		TaskChunk<Task> csvChunks = new TaskChunk<>("chunkID=baselines");

		TaskChunk<Task> cMLPlan = null;
		{
			// TODO: host, user, password, database
			try (SQLAdapter adapter = new SQLAdapter("", "", "", "")) {
				cMLPlan = TaskChunkUtil.readFromMySQLTable(adapter, "", commonFields); // TODO: Uncomment
																						// this in repo

				for (String algorithm : ALGORITHMS) {
					Map<String, String> testMap = new HashMap<>();
					testMap.put("algorithm", algorithm);
					testMap.put("subsampleRatio", String.valueOf(0.1));
					TaskChunk<Task> tmpTaskChunk = cMLPlan.select(testMap);

					tmpTaskChunk.projectRemove(new String[] { "exception" });

					tmpTaskChunk = tmpTaskChunk.group(new String[] { "dataset", "algorithm" }, new HashMap<>());
					csvChunks.addAll(tmpTaskChunk);
				}

			}
		}

		List<String> datasets = new LinkedList<>();

		/* Collect all the datasets for which we already have results */
		Set<String> datasetsInResults = new HashSet<>();
		for (Task t : cMLPlan) {
			datasetsInResults.add(t.getValueAsString("dataset"));
		}

		for (Task t : csvChunks) {
			List<Double> testErrorRates = t.getValueAsDoubleList("loss", ",");
			for (int i = 0; i < testErrorRates.size(); i++)
				testErrorRates.set(i, Math.abs(testErrorRates.get(i)));

			if (testErrorRates.size() < 2) {
				testErrorRates.add(testErrorRates.get(0));
			}
			t.store("loss", ListHelper.implode(testErrorRates, ","));
			t.store("loss_mean", StatisticsUtil.mean(t.getValueAsDoubleList("loss", ",")));
			t.store("loss_stddev", StatisticsUtil.standardDeviation(t.getValueAsDoubleList("loss", ",")));

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

		csvChunks.tTest("dataset", "algorithm", "loss", "none", "ttest");
		csvChunks.best("dataset", "algorithm", "loss_mean", "best");
		csvChunks.sort(new TaskKeyComparator(new String[] { "algorithm", "dataset" }));

		for (Task t : csvChunks) {
			t.store("entry", ValueUtil.valueToString(t.getValueAsDouble("loss_mean"), 2) + " \\begin{small}($\\pm$ "
					+ ValueUtil.valueToString(t.getValueAsDouble("loss_stddev"), 2) + ")\\end{small}");

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
			String latexTable = KVStoreUtil.kvStoreCollectionToLaTeXTable(c.toKVStoreCollection(), "algorithm",
					"dataset", "entry", "-$\\phantom{\\bullet}$");
			System.out.println(latexTable);
		}
	}
}
