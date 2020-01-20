package ai.libs.jaicore.search.testproblems.cannibals;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.problems.cannibals.CannibalProblem;
import ai.libs.jaicore.problemsets.cannibals.CannibalProblemSet;
import ai.libs.jaicore.search.exampleproblems.cannibals.CannibalProblemToGraphSearchReducer;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CannibalProblemAsGraphSearchSet extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer>, SearchGraphPath<CannibalProblem, String>, CannibalProblem, List<String>> {

	public CannibalProblemAsGraphSearchSet() {
		super("Cannibal problem as graph search", new CannibalProblemSet(), new CannibalProblemToGraphSearchReducer());
	}
}
