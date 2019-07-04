package ai.libs.jaicore.search.testproblems.cannibals;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.testproblems.cannibals.CannibalProblem;
import ai.libs.jaicore.testproblems.cannibals.CannibalProblemSet;

public class CannibalProblemAsGraphSearchSet extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer>, SearchGraphPath<CannibalProblem, String>, CannibalProblem, List<String>> {

	public CannibalProblemAsGraphSearchSet() {
		super("Cannibal problem as graph search", new CannibalProblemSet(), new CannibalProblemToGraphSearchReducer());
	}
}
