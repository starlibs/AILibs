package ai.libs.jaicore.basic.algorithm;

public interface IAlgorithmTestProblemSet<I> {

	public abstract I getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException;

	public abstract I getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException; // runtime at least 10 seconds

	public String getName();
}
