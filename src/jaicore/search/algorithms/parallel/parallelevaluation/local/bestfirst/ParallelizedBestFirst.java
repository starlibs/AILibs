package jaicore.search.algorithms.parallel.parallelevaluation.local.bestfirst;

import jaicore.search.algorithms.parallel.parallelevaluation.local.core.ParallelizedORGraphSearch;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

/**
 * Best first algorithm implementation.
 *
 * @author Felix Mohr
 */
public class ParallelizedBestFirst<T,A> extends ParallelizedORGraphSearch<T,A,Integer> {

	public ParallelizedBestFirst(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, Integer> pNodeEvaluator) {
		super(graphGenerator, pNodeEvaluator);
	}
}