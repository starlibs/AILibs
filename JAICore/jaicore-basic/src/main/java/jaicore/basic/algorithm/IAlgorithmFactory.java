package jaicore.basic.algorithm;

public interface IAlgorithmFactory<I, O> {
	public IAlgorithm<I, O> getAlgorithm();
}
