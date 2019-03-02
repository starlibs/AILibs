package jaicore.search.testproblems.nqueens;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jaicore.basic.algorithm.AlgorithmTestProblemSetForSolutionIterators;
import jaicore.search.probleminputs.GraphSearchInput;

public class NQueensProblemAsGraphSearchSet extends AlgorithmTestProblemSetForSolutionIterators<GraphSearchInput<QueenNode, String>, List<Integer>> {

	private final NQueenProblemSet problemSet = new NQueenProblemSet();
	NQueensToGraphSearchProblemInputReducer reducer = new NQueensToGraphSearchProblemInputReducer();
	
	public NQueensProblemAsGraphSearchSet() {
		super("N-Queens as graph search");
	}

	@Override
	public Map<GraphSearchInput<QueenNode, String>, Collection<List<Integer>>> getProblemsWithSolutions() {
		Map<GraphSearchInput<QueenNode, String>, Collection<List<Integer>>> problems = new HashMap<>();
		for (Entry<NQueensProblem, Collection<List<Integer>>> problemWithSolutions : problemSet.getProblemsWithSolutions().entrySet()) {
			GraphSearchInput<QueenNode, String> transformedInput = reducer.transform(problemWithSolutions.getKey());
			problems.put(transformedInput, problemWithSolutions.getValue());
		}
		return problems;
	}

	@Override
	public GraphSearchInput<QueenNode, String> getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		return reducer.transform(problemSet.getSimpleProblemInputForGeneralTestPurposes());
	}

	@Override
	public GraphSearchInput<QueenNode, String> getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		return reducer.transform(problemSet.getDifficultProblemInputForGeneralTestPurposes());
	}

}
