package jaicore.basic.algorithm;

public interface IAlgorithmTestProblemSet<I> {

	public abstract I getSimpleProblemInputForGeneralTestPurposes();

	public abstract I getDifficultProblemInputForGeneralTestPurposes(); // runtime at least 10 seconds

	public String getName();
}
