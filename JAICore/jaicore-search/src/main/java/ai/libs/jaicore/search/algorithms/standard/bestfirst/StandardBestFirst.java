package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class StandardBestFirst<N, A, V extends Comparable<V>> extends BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> {

	public StandardBestFirst(final GraphSearchWithSubpathEvaluationsInput<N, A, V> problem) {
		super(problem);
	}

	public StandardBestFirst(final IBestFirstConfig config, final GraphSearchWithSubpathEvaluationsInput<N, A, V> problem) {
		super(config, problem);
	}

	public StandardBestFirst(final IBestFirstConfig config, final GraphSearchWithSubpathEvaluationsInput<N, A, V> problem, final IPathEvaluator<N, A, V> lowerBoundEvaluator) {
		super(config, problem ,lowerBoundEvaluator);
	}

	public StandardBestFirst(final GraphSearchWithSubpathEvaluationsInput<N, A, V> problem, final IPathEvaluator<N, A, V> lowerBoundEvaluator) {
		super(problem ,lowerBoundEvaluator);
	}
}
