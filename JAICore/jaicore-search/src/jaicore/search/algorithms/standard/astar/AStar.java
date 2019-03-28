package jaicore.search.algorithms.standard.astar;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.model.probleminputs.NumberBasedAdditiveTraversalTree;

/**
 * A* algorithm implementation that is nothing else than BestFirst with a
 * specific problem input.
 *
 * @author Felix Mohr
 */
public class AStar<N, A> extends BestFirst<NumberBasedAdditiveTraversalTree<N, A>, N, A, Double> {

	public AStar(NumberBasedAdditiveTraversalTree<N, A> problem) {
		super(problem);
	}
	
}