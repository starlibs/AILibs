package jaicore.search.algorithms.standard.bestfirst;

import java.util.Random;

import jaicore.search.algorithms.GraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class RolloutBasedBestFirstTester extends GraphSearchTester {

	@Override
	public <N,A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		INodeEvaluator<N, Double> ne = new RandomCompletionBasedNodeEvaluator<>(new Random(0), 3, new AgnosticPathEvaluator<>());
		GraphSearchWithSubpathEvaluationsInput<N, A, Double> transformed = new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), ne);
		return new StandardBestFirst<>(transformed);
	}
}
