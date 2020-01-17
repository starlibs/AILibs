package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.graphsearch.problem.IPathSearch;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.PlackettLuceMCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.preferencekernel.BootstrappingPreferenceKernel;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class PLMCTSTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem, new AgnosticPathEvaluator<>());
		return new PlackettLuceMCTSPathSearch<>(newProblem, new BootstrappingPreferenceKernel<>(DescriptiveStatistics::getMean, 10), new Random(0), new Random(0));
	}

}