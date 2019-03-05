package jaicore.basic.algorithm;

public interface IAlgorithmTestProblemSet<I> {

	public abstract I getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException;

	public abstract I getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException; // runtime at least 10 seconds

	public String getName();
}
