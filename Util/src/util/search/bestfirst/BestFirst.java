package util.search.bestfirst;

import util.search.core.GraphGenerator;
import util.search.core.NodeEvaluator;
import util.search.core.ORGraphSearch;

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