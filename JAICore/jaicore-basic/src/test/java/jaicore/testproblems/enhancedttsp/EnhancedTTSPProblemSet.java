package jaicore.testproblems.enhancedttsp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.basic.algorithm.IAlgorithmTestProblemSetForSolutionIterators;

public class EnhancedTTSPProblemSet extends AAlgorithmTestProblemSet<EnhancedTTSP> implements IAlgorithmTestProblemSetForSolutionIterators<EnhancedTTSP, List<Short>> {

	public EnhancedTTSPProblemSet() {
		super("Enhanced TTSP");
	}

	private static final int MIN_N = 3;
	private static final int MAX_N = 6;

	@Override
	public EnhancedTTSP getSimpleProblemInputForGeneralTestPurposes() {
		return new EnhancedTTSPGenerator().generate(4, 100);
	}

	@Override
	public EnhancedTTSP getDifficultProblemInputForGeneralTestPurposes() {
		return new EnhancedTTSPGenerator().generate(20000, 100);
	}

	@Override
	public Map<EnhancedTTSP, Collection<List<Short>>> getProblemsWithSolutions() {
		Map<EnhancedTTSP, Collection<List<Short>>> solutions = new HashMap<>();
		EnhancedTTSPEnumeratingSolver solver = new EnhancedTTSPEnumeratingSolver();
		for (int n = MIN_N; n <= MAX_N; n++) {
			EnhancedTTSP problem = new EnhancedTTSPGenerator().generate(n, 100);
			solutions.put(problem, solver.getSolutions(problem));
		}
		return solutions;
	}
}
