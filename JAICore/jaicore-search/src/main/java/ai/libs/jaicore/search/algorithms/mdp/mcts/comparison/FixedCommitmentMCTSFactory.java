package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.function.ToDoubleFunction;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class FixedCommitmentMCTSFactory<N, A> extends MCTSFactory<N, A> {

	private ToDoubleFunction<DescriptiveStatistics> metric;
	private int k = 10;

	public ToDoubleFunction<DescriptiveStatistics> getMetric() {
		return this.metric;
	}

	public void setMetric(final ToDoubleFunction<DescriptiveStatistics> metric) {
		this.metric = metric;
	}

	public int getK() {
		return this.k;
	}

	public void setK(final int k) {
		this.k = k;
	}

	@Override
	public MCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		if (this.metric == null) {
			throw new IllegalStateException("Cannot create FixedCommitment MCTS since metric not set!");
		}
		return new FixedCommitmentMCTS<>(input, this.k, this.metric, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), this.getRandom());
	}

}
