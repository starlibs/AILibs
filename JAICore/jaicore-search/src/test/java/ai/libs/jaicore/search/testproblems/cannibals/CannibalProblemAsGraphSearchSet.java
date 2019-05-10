package ai.libs.jaicore.search.testproblems.cannibals;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.testproblems.cannibals.CannibalProblem;
import ai.libs.jaicore.testproblems.cannibals.CannibalProblemSet;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CannibalProblemAsGraphSearchSet extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer>, SearchGraphPath<CannibalProblem, String>, CannibalProblem, List<String>> {

	public CannibalProblemAsGraphSearchSet() {
		super("Cannibal problem as graph search", new CannibalProblemSet(), new CannibalProblemToGraphSearchReducer());
	}
}
