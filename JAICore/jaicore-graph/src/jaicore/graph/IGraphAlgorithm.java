package jaicore.graph;

import jaicore.basic.algorithm.IAlgorithm;

/**
 * 
 * @author fmohr
 *
 * @param <I> class of problem inputs
 * @param <O> class of solution candidates and returned solution
 * @param <P> class of the semantic problem definition established between elements of I and O
 * @param <N> class of nodes of the graph the algorithm works on
 * @param <A> class of the arcs the algorithm works on
 * @param <L> class that all listeners must belong to
 */
public interface IGraphAlgorithm<I, O, N, A> extends IAlgorithm<I, O> {

}
