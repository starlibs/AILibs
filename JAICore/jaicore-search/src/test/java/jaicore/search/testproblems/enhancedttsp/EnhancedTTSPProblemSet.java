package jaicore.search.testproblems.enhancedttsp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.basic.algorithm.IAlgorithmTestProblemSetForSolutionIterators;

public class EnhancedTTSPProblemSet extends IAlgorithmTestProblemSetForSolutionIterators<EnhancedTTSP, List<Short>> {

	public EnhancedTTSPProblemSet() {
		super("Enhanced TTSP");
	}

	private static final int MAX_N = 6;
	private static final int MAX_DISTANCE = 12;
//	private static final int TIMEOUT_IN_MS = 5 * 60 * 1000;
//	private static final boolean VISUALIZE = false;

	@Override
	public EnhancedTTSP getSimpleProblemInputForGeneralTestPurposes() {
		return new EnhancedTTSPGenerator().generate(4, 100);
	}
	
	@Override
	public EnhancedTTSP getDifficultProblemInputForGeneralTestPurposes() {
		return new EnhancedTTSPGenerator().generate(20, 100);
	}

	@Override
	public Map<EnhancedTTSP, Collection<List<Short>>> getProblemsWithSolutions() {
		Map<EnhancedTTSP, Collection<List<Short>>> solutions = new HashMap<>();
		EnhancedTTSPEnumeratingSolver solver = new EnhancedTTSPEnumeratingSolver();
		for (int n = 3; n <= MAX_N; n++) {
			EnhancedTTSP problem = new EnhancedTTSPGenerator().generate(n, 100);
			solutions.put(problem, solver.getSolutions(problem));
		}
		return solutions;
	}
}
