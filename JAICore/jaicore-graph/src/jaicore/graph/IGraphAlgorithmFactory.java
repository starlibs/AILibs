package jaicore.graph;

import jaicore.basic.algorithm.IAlgorithmFactory;

public interface IGraphAlgorithmFactory<I, O, N, A, L extends IGraphAlgorithmListener<N, A>> extends IAlgorithmFactory<I, O, L> {
	
	@Override
	public IGraphAlgorithm<I, O, N, A, L> getAlgorithm();
}
