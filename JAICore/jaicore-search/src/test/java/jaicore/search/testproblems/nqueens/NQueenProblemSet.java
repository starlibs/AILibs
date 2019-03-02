package jaicore.search.testproblems.nqueens;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmTestProblemSetForSolutionIterators;

public class NQueenProblemSet extends AlgorithmTestProblemSetForSolutionIterators<NQueensProblem, List<Integer>> {

	public NQueenProblemSet() {
		super("N-Queens");
	}

	private static final String LOGGER_NAME = "testedalgorithm";
	private static final String LOG_MESSAGE = "Checking {}-Queens Problem ... ";
	private static final String ERROR_NO_SEARCH_PRODUCED = "The factory has not returned any search object.";
	private static final String ERROR_FACTORY_NOT_SET = "Search factory has not been set";
	int[] numbersOfSolutions = { 2, 10, 4, 40}; // further numbers of solutions are 92, 352, 724

	private static final Logger logger = LoggerFactory.getLogger(NQueenProblemSet.class);
	private static final NQueensToGraphSearchProblemInputReducer reducer = new NQueensToGraphSearchProblemInputReducer();
	
	@Override
	public Map<NQueensProblem, Collection<List<Integer>>> getProblemsWithSolutions() {
		Map<NQueensProblem, Collection<List<Integer>>> problems = new HashMap<>();
		EnumeratingNQueensSolver solver = new EnumeratingNQueensSolver();
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			NQueensProblem problem = new NQueensProblem(n);
			try {
				problems.put(problem, solver.getSolutions(problem));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return problems;
	}

	@Override
	public NQueensProblem getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		return new NQueensProblem(4);
	}

	@Override
	public NQueensProblem getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		return new NQueensProblem(100);
	}
}