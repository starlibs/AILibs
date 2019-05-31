package ai.libs.jaicore.search.core.interfaces;

import ai.libs.jaicore.basic.algorithm.IAlgorithmFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public interface IOptimalPathInORGraphSearchFactory<I extends GraphSearchInput<N, A>,N, A, V extends Comparable<V>>
extends IAlgorithmFactory<I, EvaluatedSearchGraphPath<N, A, V>> {

	@Override
	public IOptimalPathInORGraphSearch<I, N, A, V> getAlgorithm();

	@Override
	public IOptimalPathInORGraphSearch<I, N, A, V> getAlgorithm(I problem);
}
