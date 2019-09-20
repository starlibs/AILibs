package ai.libs.jaicore.search.algorithms.standard.mcts.thompson;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class DNGQSampleEvent<N, A> extends AAlgorithmEvent {
	private final N node;
	private final N child;
	private final A action;
	private final double score;

	public DNGQSampleEvent(final String algorithmId, final N node, final N child, final A action, final double score) {
		super(algorithmId);
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
