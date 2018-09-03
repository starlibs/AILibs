package jaicore.basic.algorithm;

public interface IOptimizationAlgorithm<I, O, U, V extends Comparable<V>> extends IAlgorithm<I, O> {
	public IOptimizerResult<U, V> getOptimizationResult();
}
