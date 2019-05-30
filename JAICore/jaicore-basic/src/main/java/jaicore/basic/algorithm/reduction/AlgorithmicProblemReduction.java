package jaicore.basic.algorithm.reduction;

public interface AlgorithmicProblemReduction<I1, O1, I2, O2> {
	public I2 encodeProblem(I1 problem);

	public O1 decodeSolution(O2 solution);
}
