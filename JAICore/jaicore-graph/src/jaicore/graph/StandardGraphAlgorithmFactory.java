package jaicore.graph;

import jaicore.basic.algorithm.StandardAlgorithmFactory;

public abstract class StandardGraphAlgorithmFactory<I, O, N, A, L extends IGraphAlgorithmListener<N, A>> extends StandardAlgorithmFactory<I, O, L> implements IGraphAlgorithmFactory<I, O, N, A, L> {

}
