package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;

/**
 * Best first algorithm implementation.
 *
 * @author Felix Mohr
 */
public class BestFirst<T,A> extends ORGraphSearch<T,A,Double> {

	public BestFirst(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> pNodeEvaluator) {
		super(graphGenerator, pNodeEvaluator);
	}
}