package jaicore.search.testproblems.nqueens;

import java.util.List;

import jaicore.basic.algorithm.ReductionBasedAlgorithmTestProblemSet;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.testproblems.nqueens.NQueenProblemSet;
import jaicore.testproblems.nqueens.NQueensProblem;

public class NQueensProblemAsGraphSearchSet extends ReductionBasedAlgorithmTestProblemSet<GraphSearchInput<QueenNode, String>, SearchGraphPath<QueenNode, String>, NQueensProblem, List<Integer>> {

	public NQueensProblemAsGraphSearchSet() {
		super("N-Queens as graph search", new NQueenProblemSet(), new NQueensToGraphSearchReducer());
	}
}
