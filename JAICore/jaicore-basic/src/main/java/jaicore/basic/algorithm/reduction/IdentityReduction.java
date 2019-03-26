package jaicore.basic.algorithm.reduction;

public class IdentityReduction<I, O> implements AlgorithmicProblemReduction<I, O, I, O> {

	@Override
	public I encodeProblem(final I problem) {
		return problem;
	}

	@Override
	public O decodeSolution(final O solution) {
		return solution;
	}
}
