package jaicore.testproblems.knapsack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.basic.algorithm.IAlgorithmTestProblemSetForSolutionIterators;

public class KnapsackProblemSet extends AAlgorithmTestProblemSet<KnapsackProblem> implements IAlgorithmTestProblemSetForSolutionIterators<KnapsackProblem, Set<String>>  {

	public KnapsackProblemSet() {
		super("Knapsack");
	}

	@Override
	public KnapsackProblem getSimpleProblemInputForGeneralTestPurposes() {
		return KnapsackProblemGenerator.getKnapsackProblem(5);
	}

	@Override
	public KnapsackProblem getDifficultProblemInputForGeneralTestPurposes() {
		return KnapsackProblemGenerator.getKnapsackProblem(5000);
	}

	@Override
	public Map<KnapsackProblem, Collection<Set<String>>> getProblemsWithSolutions() throws InterruptedException {
		Map<KnapsackProblem, Collection<Set<String>>> problemsWithSolutions = new HashMap<>();
		EnumeratingKnapsackSolver solver = new EnumeratingKnapsackSolver();
		for (int n = 2; n <= 8; n++) {
			KnapsackProblem kp = KnapsackProblemGenerator.getKnapsackProblem(n, 0);
			problemsWithSolutions.put(kp, solver.getSolutions(kp));
		}
		return problemsWithSolutions;
	}
}
