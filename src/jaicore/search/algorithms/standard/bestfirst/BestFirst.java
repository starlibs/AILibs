package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;

/**
 * Best first algorithm implementation.
 *
 * @author Felix Mohr
 */
public class BestFirst<T,A> extends ORGraphSearch<T,A,Integer> {

	public BestFirst(GraphGenerator<T, A> graphGenerator, NodeEvaluator<T, Integer> pNodeEvaluator) {
		super(graphGenerator, pNodeEvaluator);
	}
}