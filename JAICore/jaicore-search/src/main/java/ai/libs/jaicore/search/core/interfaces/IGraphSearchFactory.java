package ai.libs.jaicore.search.core.interfaces;

import org.api4.java.algorithm.IAlgorithmFactory;

import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public interface IGraphSearchFactory<I extends GraphSearchInput<N, A>, O, N, A> extends IAlgorithmFactory<I, O> {

	@Override
	public IGraphSearch<I, O, N, A> getAlgorithm();

	@Override
	public IGraphSearch<I, O, N, A> getAlgorithm(I problem);
}
