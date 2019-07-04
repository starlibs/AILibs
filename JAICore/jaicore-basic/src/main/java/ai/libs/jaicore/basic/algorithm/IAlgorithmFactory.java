package ai.libs.jaicore.basic.algorithm;

public interface IAlgorithmFactory<I, O> {
	public IAlgorithm<I, O> getAlgorithm();

	public IAlgorithm<I, O> getAlgorithm(I input);
}
