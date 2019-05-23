package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * @author Helena Graf
 * 
 *         Interface for configuring the queue of {@link BestFirst} (and
 *         possibly other parts as well).
 *
 * @param <I>
 * @param <N>
 * @param <A>
 */
public interface IBestFirstQueueConfiguration<I extends GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> {
	public void configureBestFirst(BestFirst<I, N, A, V> bestFirst);
}
