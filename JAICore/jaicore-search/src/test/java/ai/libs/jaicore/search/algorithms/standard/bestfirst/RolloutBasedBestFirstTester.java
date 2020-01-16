package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class RolloutBasedBestFirstTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N,A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		IPathEvaluator<N, A, Double> ne = new RandomCompletionBasedNodeEvaluator<>(new Random(0), 3, new AgnosticPathEvaluator<>());
		GraphSearchWithSubpathEvaluationsInput<N, A, Double> transformed = new GraphSearchWithSubpathEvaluationsInput<>(problem, ne);
		System.out.println("1: " + ne);
		System.out.println("2: " + transformed.getPathEvaluator());
		return new StandardBestFirst<>(transformed);
	}
}
