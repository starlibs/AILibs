package ai.libs.jaicore.search.gui.plugins.mcts.dng;

public class DNGQSample {
	private final String node;
	private final String successor;
	private final double score;

	public DNGQSample(final String node, final String successor, final double score) {
		super();
		this.node = node;
		this.successor = successor;
		this.score = score;
	}

	public String getNode() {
		return this.node;
	}

	public String getSuccessor() {
		return this.successor;
	}

	public double getScore() {
		return this.score;
	}
}
