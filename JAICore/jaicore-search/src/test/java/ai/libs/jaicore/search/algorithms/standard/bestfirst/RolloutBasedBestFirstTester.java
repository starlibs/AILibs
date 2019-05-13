package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.core.interfaces.IGraphSearch;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class RolloutBasedBestFirstTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N,A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		INodeEvaluator<N, Double> ne = new RandomCompletionBasedNodeEvaluator<>(new Random(0), 3, new AgnosticPathEvaluator<>());
		GraphSearchWithSubpathEvaluationsInput<N, A, Double> transformed = new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), ne);
		return new StandardBestFirst<>(transformed);
	}
}
