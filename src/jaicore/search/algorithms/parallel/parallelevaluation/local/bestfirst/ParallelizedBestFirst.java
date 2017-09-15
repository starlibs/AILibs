package jaicore.search.algorithms.parallel.parallelevaluation.local.bestfirst;

import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ITimeoutNodeEvaluator;
import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ParallelizedORGraphSearch;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

/**
 * Best first algorithm implementation.
 *
 * @author Felix Mohr
 */
public class ParallelizedBestFirst<T, A> extends ParallelizedORGraphSearch<T, A, Integer> {

	public ParallelizedBestFirst(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Integer> pNodeEvaluator, int numThreadsForNodeEvaluation, ITimeoutNodeEvaluator<T, Integer> timeoutEvaluator,
			int timeout) {
		super(graphGenerator, pNodeEvaluator, numThreadsForNodeEvaluation, timeoutEvaluator, timeout);
	}

	public ParallelizedBestFirst(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Integer> pNodeEvaluator, int numThreadsForNodeEvaluation, int timeout) {
		super(graphGenerator, pNodeEvaluator, numThreadsForNodeEvaluation, timeout);
	}
}