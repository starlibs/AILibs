package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

/**
 * @author Helena Graf
 * 
 *         Interface for configuring the queue of {@link BestFirst} (and
 *         possibly other parts as well).
 *
 * @param <I>
 * @param <N>
 * @param <A>
 * @param <V>
 */
public interface IBestFirstQueueConfiguration<I extends GeneralEvaluatedTraversalTree<N, A, V>, N, A, V extends Comparable<V>> {
	public void configureBestFirst(BestFirst<I, N, A, V> bestFirst);
}
