package jaicore.search.algorithms.parallel.parallelevaluation.local.bestfirst;

import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ITimeoutNodeEvaluator;
import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ParallelizedORGraphSearch;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

/**
 * Best first algorithm implementation.
 *
 * @author Felix Mohr
 */
public class ParallelizedBestFirst<T, A> extends ParallelizedORGraphSearch<T, A, Integer> {

	public ParallelizedBestFirst(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, Integer> pNodeEvaluator, int numThreads, ITimeoutNodeEvaluator<T, Integer> timeoutEvaluator,
			int timeout) {
		super(graphGenerator, pNodeEvaluator, numThreads, timeoutEvaluator, timeout);
	}

	public ParallelizedBestFirst(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, Integer> pNodeEvaluator, int numThreads, int timeout) {
		super(graphGenerator, pNodeEvaluator, numThreads, timeout);
	}
}