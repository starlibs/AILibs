package ai.libs.jaicore.problemsets.enhancedttsp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSetForSolutionIterators;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPEnumeratingSolver;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPGenerator;
import ai.libs.jaicore.problems.enhancedttsp.locationgenerator.RandomLocationGenerator;

public class EnhancedTTSPProblemSet extends AAlgorithmTestProblemSet<EnhancedTTSP> implements IAlgorithmTestProblemSetForSolutionIterators<EnhancedTTSP, List<Short>> {

	public EnhancedTTSPProblemSet() {
		super("Enhanced TTSP");
	}

	private static final int MIN_N = 3;
	private static final int MAX_N = 6;

	@Override
	public EnhancedTTSP getSimpleProblemInputForGeneralTestPurposes() {
		return new EnhancedTTSPGenerator(new RandomLocationGenerator(new Random(0))).generate(4, 100, 0);
	}

	@Override
	public EnhancedTTSP getDifficultProblemInputForGeneralTestPurposes() {
		return new EnhancedTTSPGenerator(new RandomLocationGenerator(new Random(0))).generate(20000, 100, 0);
	}

	@Override
	public Map<EnhancedTTSP, Collection<List<Short>>> getProblemsWithSolutions() {
		Map<EnhancedTTSP, Collection<List<Short>>> solutions = new HashMap<>();
		EnhancedTTSPEnumeratingSolver solver = new EnhancedTTSPEnumeratingSolver();
		for (int n = MIN_N; n <= MAX_N; n++) {
			EnhancedTTSP problem = new EnhancedTTSPGenerator(new RandomLocationGenerator(new Random(0))).generate(n, 100, 0);
			solutions.put(problem, solver.getSolutions(problem));
		}
		return solutions;
	}
}
