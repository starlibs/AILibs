package ai.libs.jaicore.basic.algorithm;

import ai.libs.jaicore.basic.ScoredItem;

public interface IOptimizationAlgorithmFactory<I,O extends ScoredItem<V>, V extends Comparable<V>> extends IAlgorithmFactory<I, O> {

	@Override
	public IOptimizationAlgorithm<I, O, V> getAlgorithm();

	@Override
	public IOptimizationAlgorithm<I, O, V> getAlgorithm(I input);
}
