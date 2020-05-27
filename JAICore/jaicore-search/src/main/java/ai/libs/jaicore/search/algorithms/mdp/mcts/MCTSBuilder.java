package ai.libs.jaicore.search.algorithms.mdp.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class MCTSBuilder<N, A> {
	private IMDP<N, A, Double> mdp;
	private double gamma = 1.0;
	private double epsilon = 0.01;
	private int maxIterations = 1000;
	private IPathUpdatablePolicy<N, A, Double> treePolicy;
	private IPolicy<N, A> defaultPolicy = new UniformRandomPolicy<>();


	public MCTSBuilder<N, A> withMDP(final IMDP<N, A, Double> mdp) {
		this.mdp = mdp;
		return this;
	}

	public MCTSBuilder<N, A> withGamma(final double gamma) {
		this.gamma = gamma;
		return this;
	}

	public MCTSBuilder<N, A> withMaxIterations(final int maxIterations) {
		this.maxIterations = maxIterations;
		return this;
	}

	public MCTS<N, A> build() {
		if (this.treePolicy == null) {
			this.treePolicy = new UCBPolicy<>(this.mdp.isMaximizing());
		}
		return new MCTS<>(this.mdp, this.treePolicy, this.defaultPolicy, this.maxIterations, this.gamma, this.epsilon);
	}
}
