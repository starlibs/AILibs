package ai.libs.jaicore.search.testproblems.nqueens;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import ai.libs.jaicore.problems.nqueens.NQueensProblem;
import ai.libs.jaicore.problemsets.nqueens.NQueenProblemSet;
import ai.libs.jaicore.search.exampleproblems.nqueens.QueenNode;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public class NQueensProblemAsGraphSearchSet extends ReductionBasedAlgorithmTestProblemSet<GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>, NQueensProblem, List<Integer>> {

	public NQueensProblemAsGraphSearchSet() {
		super("N-Queens as graph search", new NQueenProblemSet(), new NQueensToGraphSearchReducer());
	}
}
