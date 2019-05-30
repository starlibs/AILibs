package jaicore.search.core.interfaces;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.search.probleminputs.GraphSearchInput;

/**
 * Graph search algorithms take a graph <N, A> that is given in the form of a graph generator and search it.
 * Usually, the algorithm uses internal wrapper classes to represent edges and nodes, which is why there are
 * additional generics for that.
 * 
 * @author fmohr
 * 
 * @param <I>
 * @param <O>
 * @param <N>
 * @param <A>
 */
public interface IGraphSearch<I extends GraphSearchInput<N, A>, O, N, A> extends IAlgorithm<I, O> {
	public GraphGenerator<N, A> getGraphGenerator();
}
