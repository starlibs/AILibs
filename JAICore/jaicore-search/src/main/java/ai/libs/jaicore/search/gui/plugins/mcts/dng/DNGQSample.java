package ai.libs.jaicore.search.gui.plugins.mcts.dng;

public class DNGQSample {
	private final String node;
	private final String action;
	private final double score;

	public DNGQSample(final String node, final String action, final double score) {
		super();
		this.node = node;
		this.action = action;
		this.score = score;
	}

	public String getNode() {
		return this.node;
	}

	public String getSuccessor() {
		return this.action;
	}

	public double getScore() {
		return this.score;
	}
}
