package jaicore.testproblems.npuzzle;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;

public class NPuzzleProblemSet extends AAlgorithmTestProblemSet<NPuzzleProblem> {

	public NPuzzleProblemSet(final String name) {
		super("N-Puzzle");
	}

	@Override
	public NPuzzleProblem getSimpleProblemInputForGeneralTestPurposes() {
		return new NPuzzleProblem(3, 0);
	}
	@Override
	public NPuzzleProblem getDifficultProblemInputForGeneralTestPurposes() {
		return new NPuzzleProblem(1000, 0);
	}
}
