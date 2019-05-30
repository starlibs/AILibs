package hasco.knowledgebase;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
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
	private String benchmarkName;
	private String testEvalTechnique;
	private String testSplitTechnique;
	private String valEvalTechnique;
	private String valSplitTechnique;
	private int testSeed;
	private int valSeed;

	public IntermediateResultHandler(final SQLAdapter adapter, final String benchmarkName, final String testEvalTechnique, final String testSplitTechnique, final int testSeed, final String valEvalTechnique, final String valSplitTechnique,
			final int valSeed) {
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
	public void receiveSolutionEvaluationEvent(final HASCOSolutionEvaluationEvent<?, ?> solution) throws JsonProcessingException, SQLException {
		Map<String, String> map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		String composition = mapper.writeValueAsString(solution.getComposition());
		map.put("composition", composition);
		//// outer split
		map.put("test_evaluation_technique", this.testEvalTechnique);
		map.put("test_split_technique", this.testSplitTechnique);
		map.put("test_seed", Integer.toString(this.testSeed));
		//// inner split
		map.put("val_evaluation_technique", this.valEvalTechnique);
		map.put("val_split_technique", this.valSplitTechnique);
		map.put("val_seed", Integer.toString(this.valSeed));
		map.put("error_rate", solution.getScore().toString());
		// training and test time, have to get myself with apache stopwatch
		map.put("dataset", this.benchmarkName);
		if (this.adapter != null) {
			this.adapter.insert(TABLE_NAME, map);
		}
	}
}
