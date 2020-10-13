package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.Random;
import java.util.function.ToDoubleFunction;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class FixedCommitmentMCTS<N, A> extends MCTS<N, A> {

	public FixedCommitmentMCTS(final IMDP<N, A, Double> input, final int k, final ToDoubleFunction<DescriptiveStatistics> metric, final int maxIterations, final double gamma, final double epsilon, final Random random, final boolean tabooExhaustedNodes) {
		super(input, new FixedCommitmentPolicy<>(k, metric), new UniformRandomPolicy<>(random), maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}
}
