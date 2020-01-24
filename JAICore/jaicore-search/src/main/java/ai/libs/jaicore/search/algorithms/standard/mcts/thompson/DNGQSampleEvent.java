package ai.libs.jaicore.search.algorithms.standard.mcts.thompson;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class DNGQSampleEvent<N, A> extends AAlgorithmEvent {
	private final N node;
	private final N child;
	private final A action;
	private final double score;

	public DNGQSampleEvent(final IAlgorithm<?, ?> algorithm, final N node, final N child, final A action, final double score) {
		super(algorithm);
		this.node = node;
		this.child = child;
		this.action = action;
		this.score = score;
	}

	public N getNode() {
		return this.node;
	}

	public A getAction() {
		return this.action;
	}

	public double getScore() {
		return this.score;
	}

	public N getChild() {
		return this.child;
	}

}
