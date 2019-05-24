package hasco.knowledgebase;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;

import hasco.events.HASCOSolutionEvaluationEvent;
import jaicore.basic.SQLAdapter;

/**
 * Class to handle HASCOSolutionEvaluationEvents and insert the results into the
 * database.
 * 
 * @author jmhansel
 *
 */
public class IntermediateResultHandler {

	public static final String TABLE_NAME = "performance_samples";

	private SQLAdapter adapter;
	private String benchmarkName, testEvalTechnique, testSplitTechnique, valEvalTechnique, valSplitTechnique;
	private int testSeed, valSeed;

	public IntermediateResultHandler(SQLAdapter adapter, String benchmarkName, String testEvalTechnique,
			String testSplitTechnique, int testSeed, String valEvalTechnique, String valSplitTechnique, int valSeed) {
		this.adapter = adapter;
		this.benchmarkName = benchmarkName;
		this.testEvalTechnique = testEvalTechnique;
		this.testSplitTechnique = testSplitTechnique;
		this.testSeed = testSeed;
		this.valEvalTechnique = valEvalTechnique;
		this.valSplitTechnique = valSplitTechnique;
		this.valSeed = valSeed;
	}

	@Subscribe
	public void receiveSolutionEvaluationEvent(final HASCOSolutionEvaluationEvent solution) {
		try {
			System.out.println("Handling Event");
			Map<String, String> map = new HashMap<>();
			// map.put("run_id", "" + this.runId);
			ObjectMapper mapper = new ObjectMapper();
			String composition = mapper.writeValueAsString(solution.getComposition());
			map.put("composition", composition);
			// map.put("dataset_id", );
			//// origin
			// map.put("dataset_origin", );
			//// outer split
			map.put("test_evaluation_technique", testEvalTechnique);
			map.put("test_split_technique", testSplitTechnique);
			map.put("test_seed", Integer.toString(testSeed));
			//// inner split
			map.put("val_evaluation_technique", valEvalTechnique);
			map.put("val_split_technique", valSplitTechnique);
			map.put("val_seed", Integer.toString(valSeed));
			map.put("error_rate", solution.getScore().toString());
			// training and test time, have to get myself with apache stopwatch
			// map.put("training_time", solution.get);
			map.put("dataset", benchmarkName);
			System.out.println(map);
			if (adapter != null)
				this.adapter.insert(TABLE_NAME, map);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
