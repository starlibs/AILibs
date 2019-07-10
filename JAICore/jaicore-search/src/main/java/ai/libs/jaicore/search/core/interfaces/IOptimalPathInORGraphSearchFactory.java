package ai.libs.jaicore.search.core.interfaces;

import org.api4.java.algorithm.IOptimizationAlgorithmFactory;

import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public interface IOptimalPathInORGraphSearchFactory<I extends GraphSearchInput<N, A>,N, A, V extends Comparable<V>>
extends IOptimizationAlgorithmFactory<I, EvaluatedSearchGraphPath<N, A, V>, V> {

	@Override
	public IOptimalPathInORGraphSearch<I, N, A, V> getAlgorithm();

	@Override
	public IOptimalPathInORGraphSearch<I, N, A, V> getAlgorithm(I problem);
}
