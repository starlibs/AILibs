package jaicore.search.core.interfaces;

import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.search.probleminputs.GraphSearchInput;

public interface IGraphSearchFactory<I extends GraphSearchInput<N, A>, O, N, A> extends IAlgorithmFactory<I, O> {

	@Override
	public IGraphSearch<I, O, N, A> getAlgorithm();
	
	@Override
	public IGraphSearch<I, O, N, A> getAlgorithm(I problem);
}
