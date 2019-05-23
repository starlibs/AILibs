package jaicore.search.testproblems.cannibals;

import java.util.List;

import jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.testproblems.cannibals.CannibalProblem;
import jaicore.testproblems.cannibals.CannibalProblemSet;

public class CannibalProblemAsGraphSearchSet extends ReductionBasedAlgorithmTestProblemSet<GraphSearchWithPathEvaluationsInput<CannibalProblem, String, Integer>, SearchGraphPath<CannibalProblem, String>, CannibalProblem, List<String>> {

	public CannibalProblemAsGraphSearchSet() {
		super("Cannibal problem as graph search", new CannibalProblemSet(), new CannibalProblemToGraphSearchReducer());
	}
}
