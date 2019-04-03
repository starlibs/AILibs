package jaicore.search.algorithms.standard.uncertainty;

import java.util.Random;

import jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig.OversearchAvoidanceMode;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.AgnosticPathEvaluator;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithUncertaintyBasedSubpathEvaluationInput;

public class TwoPhaseTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		OversearchAvoidanceConfig<N, Double> config = new OversearchAvoidanceConfig<>(OversearchAvoidanceMode.TWO_PHASE_SELECTION, 0);
		UncertaintyORGraphSearchFactory<N, A, Double> searchFactory = new UncertaintyORGraphSearchFactory<>();
		searchFactory.setConfig(config);
		RandomCompletionBasedNodeEvaluator<N, A, Double> rcne = new RandomCompletionBasedNodeEvaluator<>(new Random(0), 3, new AgnosticPathEvaluator<>());
		rcne.setUncertaintySource(new BasicUncertaintySource<>());
		GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, Double> transformedProblem = new GraphSearchWithUncertaintyBasedSubpathEvaluationInput<>(problem.getGraphGenerator(), rcne);
		searchFactory.setProblemInput(transformedProblem);
		return searchFactory.getAlgorithm();
	}
}
