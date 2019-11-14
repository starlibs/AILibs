package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.Random;
import java.util.function.Function;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;

public class FixedCommitmentMCTSPathSearch<I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I ,N, A, Double>{

	public FixedCommitmentMCTSPathSearch(final I problem, final Double penaltyForFailedEvaluation, final int k, final Function<DescriptiveStatistics, Double> metric) {
		super(problem, new FixedCommitmentPolicy<>(k, metric), new UniformRandomPolicy<>(new Random(0)), penaltyForFailedEvaluation);
	}

}
