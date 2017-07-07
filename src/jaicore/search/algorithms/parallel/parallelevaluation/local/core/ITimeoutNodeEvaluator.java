package jaicore.search.algorithms.parallel.parallelevaluation.local.core;

import jaicore.search.structure.core.Node;

/**
 * This interface is used to create f-values for nodes that ran into an timeout when computing f. So this is an escape-value for f.
 * 
 * @author Felix Mohr
 *
 */
public interface ITimeoutNodeEvaluator<T, V extends Comparable<V>> {

	/**
	 * A typical behavior would be to return a very bad value that makes an exploration of the node unlikely.
	 * 
	 * If NULL is returned, then the node is not inserted into OPEN at all, so this can be used to completely deactivate exploration.
	 * In fact, the NULL-evaluator is the default implementation since due to the generic nature of V no other choice is possible.
	 * 
	 * @param node
	 * @return
	 */
	public V f(Node<T, V> node);
}
