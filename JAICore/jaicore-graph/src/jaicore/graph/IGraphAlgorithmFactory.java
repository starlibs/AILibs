package jaicore.graph;

import jaicore.basic.algorithm.IAlgorithmFactory;

public interface IGraphAlgorithmFactory<I, O, N, A> extends IAlgorithmFactory<I, O> {
	
	@Override
	public IGraphAlgorithm<I, O, N, A> getAlgorithm();
}
