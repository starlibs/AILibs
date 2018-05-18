package ida2018.collectors;

import java.util.HashMap;
import java.util.Map;

import ida2018.IDA2018Util;
import jaicore.basic.MySQLAdapter;
import jaicore.basic.StatisticsUtil;
import jaicore.basic.ValueUtil;
import jaicore.basic.chunks.Task;
import jaicore.basic.chunks.TaskChunk;
import jaicore.basic.chunks.TaskChunkUtil;
import jaicore.basic.chunks.TaskKeyComparator;
import jaicore.basic.kvstore.KVStoreUtil;

public class Table2CollectorWithChunks {


	
	public static void main(String[] args) throws Exception {

		MySQLAdapter adapter = IDA2018Util.getAdapter();
		
		int minSamples = 2;

		/* read results for individual */
		Map<String, String> individualInitMap = new HashMap<>();
		individualInitMap.put("candidate", "individual");
		TaskChunk<Task> individualTaskChunk = TaskChunkUtil.readFromMySQLTable(adapter, "IndividualPerformances", individualInitMap);
		Map<String,String> toRemove = new HashMap<>();
		toRemove.put("errorRate", "-1");
		individualTaskChunk.removeAny(toRemove, true);
		toRemove.put("errorRate", "NULL");
		individualTaskChunk.removeAny(toRemove, true);
		individualTaskChunk = individualTaskChunk.group(new String[] { "dataset", "classifier", "candidate" }, new HashMap<>());
		individualTaskChunk.removeGroupsIfNotAtLeastWithSize(minSamples);
		for (Task t : individualTaskChunk) {
			t.store("mean", StatisticsUtil.mean(t.getValueAsDoubleList("errorRate", ",")));
		}
		individualTaskChunk.singleBest("dataset", "classifier", "mean");
		
		
		/* read results for homogeneous */
		Map<String, String> homoInitMap = new HashMap<>();
		homoInitMap.put("candidate", "homogeneous");
		TaskChunk<Task> homoTaskChunk = TaskChunkUtil.readFromMySQLTable(adapter, "RandomBestOfKHomogeneousStumpPerformances", homoInitMap);
		homoTaskChunk = homoTaskChunk.group(new String[] { "dataset", "classifier", "candidate" }, new HashMap<>());
		homoTaskChunk.removeGroupsIfNotAtLeastWithSize(minSamples);
		for (Task t : homoTaskChunk) {
			t.store("mean", StatisticsUtil.mean(t.getValueAsDoubleList("errorRate", ",")));
		}
		homoTaskChunk.singleBest("dataset", "classifier", "mean");

		/* read results for heterogeneous */
		Map<String, String> heteroInitMap = new HashMap<>();
		heteroInitMap.put("candidate", "heterogeneous");
		TaskChunk<Task> heteroTaskChunk = TaskChunkUtil.readFromMySQLTable(adapter, "RandomBestOfKHeterogeneousStumpPerformances", heteroInitMap);
		heteroTaskChunk = heteroTaskChunk.group(new String[] { "dataset", "left_classifier", "inner_classifier", "right_classifier", "candidate" }, new HashMap<>());
		heteroTaskChunk.removeGroupsIfNotAtLeastWithSize(minSamples);
		for (Task t : heteroTaskChunk) {
			t.store("id", t.getValueAsString("left_classifier") + "/" + t.getValueAsString("inner_classifier") + "/" + t.getValueAsString("right_classifier"));
			t.store("mean", StatisticsUtil.mean(t.getValueAsDoubleList("errorRate", ",")));
		}
		heteroTaskChunk.singleBest("dataset", "id", "mean");
		
		
		/* read results for bagging */
		Map<String, String> baggingInitMap = new HashMap<>();
		baggingInitMap.put("candidate", "bagging");
		TaskChunk<Task> baggingTaskChunk = TaskChunkUtil.readFromMySQLTable(adapter, "baggedclassifiers", baggingInitMap);
		toRemove.put("errorRate", "-1");
		baggingTaskChunk.removeAny(toRemove, true);
		toRemove.put("errorRate", null);
		baggingTaskChunk.removeAny(toRemove, true);
		baggingTaskChunk = baggingTaskChunk.group(new String[] { "dataset", "classifier", "candidate" }, new HashMap<>());
		baggingTaskChunk.removeGroupsIfNotAtLeastWithSize(minSamples);
		for (Task t : baggingTaskChunk) {
			t.store("mean", StatisticsUtil.mean(t.getValueAsDoubleList("errorRate", ",")));
		}
		baggingTaskChunk.singleBest("dataset", "classifier", "mean");

		/* read results for ensembles */
		Map<String, String> ensemblesInitMap = new HashMap<>();
		ensemblesInitMap.put("candidate", "ensembles");
		TaskChunk<Task> ensemblesTaskChunk = TaskChunkUtil.readFromMySQLResultSet(adapter.getResultsOfQuery("SELECT substring_index(substring_index(dataset,'/',-1),'\\\\',-1) as dataset, classifier,seed,errorRate FROM homogeneousensemblesofreductionstumps"), ensemblesInitMap);
		toRemove.put("errorRate", "-1");
		ensemblesTaskChunk.removeAny(toRemove, true);
		toRemove.put("errorRate", null);
		ensemblesTaskChunk.removeAny(toRemove, true);
		ensemblesTaskChunk = ensemblesTaskChunk.group(new String[] { "dataset", "classifier", "candidate" }, new HashMap<>());
		ensemblesTaskChunk.removeGroupsIfNotAtLeastWithSize(minSamples);
		for (Task t : ensemblesTaskChunk) {
			t.store("mean", StatisticsUtil.mean(t.getValueAsDoubleList("errorRate", ",")));
		}
		ensemblesTaskChunk.singleBest("dataset", "classifier", "mean");
		
				
		/* now conduct a significance test for the individual and ensemble methods respectively */
		TaskChunk<Task> unitedIndividualChunks = new TaskChunk<>("chunkID=unitedIndividual");
		TaskChunk<Task> unitedEnsembleChunks = new TaskChunk<>("chunkID=unitedEnsemble");
		for (String dataset : IDA2018Util.getConsideredDatasets()) {
			Map<String,String> dsSelection = new HashMap<>();
			dsSelection.put("dataset", dataset);
			unitedIndividualChunks.addAll(individualTaskChunk.select(dsSelection));
			unitedIndividualChunks.addAll(heteroTaskChunk.select(dsSelection));
			unitedIndividualChunks.addAll(homoTaskChunk.select(dsSelection));
			unitedEnsembleChunks.addAll(baggingTaskChunk.select(dsSelection));
			unitedEnsembleChunks.addAll(ensemblesTaskChunk.select(dsSelection));
		}
		unitedIndividualChunks.tTest("dataset", "candidate", "errorRate", "individual", "tTest");
		unitedEnsembleChunks.tTest("dataset", "candidate", "errorRate", "bagging", "tTest");
		
		/* now merge all results */
		TaskChunk<Task> unitedChunks = new TaskChunk<>("chunkID=unitedChunks");
		unitedChunks.addAll(unitedIndividualChunks);
		unitedChunks.addAll(unitedEnsembleChunks);
		
		/* determine globally best per dataset */
		unitedChunks.best("dataset", "candidate", "mean");
		unitedChunks.bestTTest("dataset", "candidate", "errorRate", "overallTTest");
		
		/* format and output table */
		for (Task t : unitedChunks) {
			StringBuilder sb = new StringBuilder();
			String val = ValueUtil.valueToString(t.getValueAsDouble("mean") * 100, 2) + "$\\pm$"
					+ ValueUtil.valueToString(StatisticsUtil.standardDeviation(t.getValueAsDoubleList("errorRate", ",")) * 100, 2);
			if (t.getValueAsBoolean("best"))
				val = "\\textbf{" + val + "}";
			
			
			/* global t-test */
			if (t.containsKey("overallTTest") && t.getValueAsString("overallTTest").equals("eq")) {
				val = "\\underline{" + val + "}";
			}
			
			sb.append(val);
			
			/* group-wise t-test */
			if (t.containsKey("tTest")) {
				switch (t.getValueAsString("tTest")) {
				case "impr":
					sb.append(" $\\circ$");
					break;
				case "deg":
					sb.append(" $\\bullet$");
					break;
				default:
					sb.append(" $\\phantom{\\circ}$");
					break;
				}
			}
			else
				sb.append(" $\\phantom{\\circ}$");
			t.store("entry", sb.toString());
		}
		unitedChunks.sort(new TaskKeyComparator(new String[] {"dataset", "candidate"}));
		System.out.println(KVStoreUtil.kvStoreCollectionToLaTeXTable(unitedChunks.toKVStoreCollection(), "dataset", "candidate", "entry"));
	}
}
