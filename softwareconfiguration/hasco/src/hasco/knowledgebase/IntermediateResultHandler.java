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

	public static final String TABLE_NAME = "intermediate_results";

	private SQLAdapter adapter;
	private String benchmarkName;

	public IntermediateResultHandler(SQLAdapter adapter, String benchmarkName) {
		this.adapter = adapter;
		this.benchmarkName = benchmarkName;
	}

	@Subscribe
	public void receiveSolutionEvaluationEvent(final HASCOSolutionEvaluationEvent solution) {
		try {
			System.out.println("Handling Event");
			Map<String, String> map = new HashMap<>();
			// map.put("run_id", "" + this.runId);
			ObjectMapper mapper = new ObjectMapper();
			String composition = mapper.writeValueAsString(solution.getComposition());
//			map.put("pipeline", composition);
//			map.put("dataset_id", );
////			origin 
//			map.put("dataset_origin", );
////			outer split
//			map.put("test_evaluation_technique", );
//			map.put("test_split_technique", );
//			map.put("test_seed", );
////			inner split
//			map.put("val_evaluation_technique", );
//			map.put("val_split_technique", );
//			map.put("val_seed", );
//			map.put("error_rate", solution.getScore().toString());
//			training and test time, have to get myself with apache stopwatch
//			map.put("training_time", solution.get);
			
			map.put("dataset", benchmarkName);
			map.put("error_rate", solution.getScore().toString());


			System.out.println(map);
			if (adapter != null)
				this.adapter.insert(TABLE_NAME, map);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
