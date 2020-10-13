package ai.libs.jaicore.search.algorithms.standard.uncertainty;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig.OversearchAvoidanceMode;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithUncertaintyBasedSubpathEvaluationInput;

public class ParetoSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		OversearchAvoidanceConfig<N, A, Double> config = new OversearchAvoidanceConfig<>(OversearchAvoidanceMode.PARETO_FRONT_SELECTION, 0);
		UncertaintyORGraphSearchFactory<N, A, Double> searchFactory = new UncertaintyORGraphSearchFactory<>();
		searchFactory.setConfig(config);
		RandomCompletionBasedNodeEvaluator<N, A, Double> rcne = new RandomCompletionBasedNodeEvaluator<>(new Random(0), 3, new AgnosticPathEvaluator<>());
		rcne.setUncertaintySource(new BasicUncertaintySource<>());
		GraphSearchWithUncertaintyBasedSubpathEvaluationInput<N, A, Double> transformedProblem = new GraphSearchWithUncertaintyBasedSubpathEvaluationInput<>(problem, rcne);
		searchFactory.setProblemInput(transformedProblem);
		return searchFactory.getAlgorithm();
	}
}
