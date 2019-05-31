package ai.libs.jaicore.search.algorithms.standard.astar;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;

/**
 * A* algorithm implementation that is nothing else than BestFirst with a
 * specific problem input.
 *
 * @author Felix Mohr
 */
public class AStar<N, A> extends BestFirst<GraphSearchWithNumberBasedAdditivePathEvaluation<N, A>, N, A, Double> {

	public AStar(GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> problem) {
		super(problem);
	}
	
}