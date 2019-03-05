package jaicore.basic.algorithm;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;

public class ReductionBasedAlgorithmTestProblemSet<I1, O1, I2, O2> extends AAlgorithmTestProblemSet<I1> {

	private final IAlgorithmTestProblemSet<I2> mainProblemSet;
	private final AlgorithmicProblemReduction<I2, O2, I1, O1> reduction;

	public ReductionBasedAlgorithmTestProblemSet(final String name, final IAlgorithmTestProblemSet<I2> mainProblemSet, final AlgorithmicProblemReduction<I2, O2, I1, O1> reduction) {
		super(name);
		this.mainProblemSet = mainProblemSet;
		this.reduction = reduction;
	}

	public IAlgorithmTestProblemSet<I2> getMainProblemSet() {
		return this.mainProblemSet;
	}

	public AlgorithmicProblemReduction<I2, O2, I1, O1> getReduction() {
		return this.reduction;
	}

	@Override
	public I1 getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return this.reduction.encodeProblem(this.mainProblemSet.getSimpleProblemInputForGeneralTestPurposes());
	}

	@Override
	public I1 getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		return this.reduction.encodeProblem(this.mainProblemSet.getDifficultProblemInputForGeneralTestPurposes());
	}
}
