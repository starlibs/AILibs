package jaicore.testproblems.cannibals;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.basic.algorithm.IAlgorithmTestProblemSetForSolutionIterators;

public class CannibalProblemSet extends AAlgorithmTestProblemSet<CannibalProblem> implements IAlgorithmTestProblemSetForSolutionIterators<CannibalProblem, List<String>>  {

	public CannibalProblemSet() {
		super("Cannibal-Missionaries");
	}

	@Override
	public CannibalProblem getSimpleProblemInputForGeneralTestPurposes() {
		return new CannibalProblem(true, 3, 3, 0, 0);
	}

	@Override
	public CannibalProblem getDifficultProblemInputForGeneralTestPurposes() {
		return new CannibalProblem(true, 1000, 1000, 0, 0);
	}

	@Override
	public Map<CannibalProblem, Collection<List<String>>> getProblemsWithSolutions() throws InterruptedException {
		Map<CannibalProblem, Collection<List<String>>> problemsWithSolutions = new HashMap<>();
		return problemsWithSolutions;
	}
}
