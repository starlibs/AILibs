package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.Random;

import ai.libs.jaicore.basic.algorithm.AAlgorithmFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public abstract class MCTSFactory<N, A, B extends MCTSFactory<N, A, B>> extends AAlgorithmFactory<IMDP<N, A, Double>, IPolicy<N, A>, MCTS<N, A>> {

	private int maxIterations = Integer.MAX_VALUE;
	private double gamma = 1.0; // a gamma value of 1 means that there is no discount
	private double epsilon = 0.0;
	private Random random = new Random(0);
	private boolean tabooExhaustedNodes = false;
	private boolean maximize = false;

	public int getMaxIterations() {
		return this.maxIterations;
	}

	public B withMaxIterations(final int maxIterations) {
		this.maxIterations = maxIterations;
		return this.getSelf();
	}

	public double getGamma() {
		return this.gamma;
	}

	public B withGamma(final double gamma) {
		this.gamma = gamma;
		return this.getSelf();
	}

	public double getEpsilon() {
		return this.epsilon;
	}

	public B withEpsilon(final double epsilon) {
		this.epsilon = epsilon;
		return this.getSelf();
	}

	public B maximize() {
		this.maximize = true;
		return this.getSelf();
	}

	public B minimize() {
		this.maximize = false;
		return this.getSelf();
	}

	public boolean isMaximize() {
		return this.maximize;
	}

	public Random getRandom() {
		return this.random;
	}

	public B withRandom(final Random random) {
		this.random = random;
		return this.getSelf();
	}

	public boolean isTabooExhaustedNodes() {
		return this.tabooExhaustedNodes;
	}

	public B withTabooExhaustedNodes(final boolean tabooExhaustedNodes) {
		this.tabooExhaustedNodes = tabooExhaustedNodes;
		return this.getSelf();
	}



	@Override
	public MCTS<N, A> getAlgorithm() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public B getSelf() {
		return (B)this;
	}
}
