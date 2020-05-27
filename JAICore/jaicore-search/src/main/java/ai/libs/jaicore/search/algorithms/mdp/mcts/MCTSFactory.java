package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.Random;

import ai.libs.jaicore.basic.algorithm.AAlgorithmFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public abstract class MCTSFactory<N, A> extends AAlgorithmFactory<IMDP<N, A, Double>, IPolicy<N, A>, MCTS<N, A>> {

	private int maxIterations = 10000;
	private double gamma = 1.0;
	private double epsilon = 0.0;
	private Random random = new Random(0);

	public int getMaxIterations() {
		return this.maxIterations;
	}

	public void setMaxIterations(final int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public double getGamma() {
		return this.gamma;
	}

	public void setGamma(final double gamma) {
		this.gamma = gamma;
	}

	public double getEpsilon() {
		return this.epsilon;
	}

	public void setEpsilon(final double epsilon) {
		this.epsilon = epsilon;
	}

	public Random getRandom() {
		return this.random;
	}

	public void setRandom(final Random random) {
		this.random = random;
	}

	@Override
	public MCTS<N, A> getAlgorithm() {
		throw new UnsupportedOperationException();
	}
}
