package jaicore.search.core.interfaces;

import jaicore.basic.algorithm.AAlgorithmFactory;
import jaicore.search.probleminputs.GraphSearchInput;

public abstract class StandardORGraphSearchFactory<I extends GraphSearchInput<N, A>, O, N, A, V extends Comparable<V>> extends AAlgorithmFactory<I, O> implements IGraphSearchFactory<I, O, N, A> {

}
