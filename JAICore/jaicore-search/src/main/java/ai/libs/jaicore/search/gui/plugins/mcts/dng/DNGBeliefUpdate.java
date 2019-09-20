package ai.libs.jaicore.search.gui.plugins.mcts.dng;

public class DNGBeliefUpdate {

	private final String node;
	private final double mu;
	private final double alpha;
	private final double beta;
	private final double lambda;

	public DNGBeliefUpdate(final String node, final double mu, final double alpha, final double beta, final double lambda) {
		super();
		this.node = node;
		this.mu = mu;
		this.alpha = alpha;
		this.beta = beta;
		this.lambda = lambda;
	}

	public String getNode() {
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
