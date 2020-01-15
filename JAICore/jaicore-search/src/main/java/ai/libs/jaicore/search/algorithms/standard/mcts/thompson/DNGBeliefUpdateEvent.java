package ai.libs.jaicore.search.algorithms.standard.mcts.thompson;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class DNGBeliefUpdateEvent<N> extends AAlgorithmEvent {

	private final N node;
	private final double mu;
	private final double alpha;
	private final double beta;
	private final double lambda;

	public DNGBeliefUpdateEvent(final IAlgorithm<?, ?> algorithm, final N node, final double mu, final double alpha, final double beta, final double lambda) {
		super(algorithm);
		this.node = node;
		this.mu = mu;
		this.alpha = alpha;
		this.beta = beta;
		this.lambda = lambda;
	}

	public N getNode() {
		return this.node;
	}

	public double getMu() {
		return this.mu;
	}

	public double getAlpha() {
		return this.alpha;
	}

	public double getBeta() {
		return this.beta;
	}

	public double getLambda() {
		return this.lambda;
	}
}
