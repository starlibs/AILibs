package jaicore.search.algorithms.standard.astar;

import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;

public class AStarFactory<T, A> extends BestFirstFactory<GraphSearchWithNumberBasedAdditivePathEvaluation<T, A>, T, A, Double> {

	public AStarFactory() {
		super();
	}

	public AStarFactory(final int timeoutForFInMS) {
		super(timeoutForFInMS);
	}

	@Override
	public AStar<T, A> getAlgorithm() {
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public AStar<T, A> getAlgorithm(final GraphSearchWithNumberBasedAdditivePathEvaluation<T, A> input) {
		AStar<T, A> search = new AStar<>(input);
		this.setupAlgorithm(search);
		return search;
	}
}
