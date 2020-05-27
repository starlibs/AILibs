package ai.libs.jaicore.search.algorithms.mdp.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.old.UniformRandomPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class MCTSPolicySearchBuilder<N, A> {
	private IMDP<N, A, Double> mdp;
	private double gamma = 1.0;
	private double epsilon = 0.01;
	private int maxIterations = 1000;
	private IPathUpdatablePolicy<N, A, Double> treePolicy;
	private IPolicy<N, A> defaultPolicy = new UniformRandomPolicy<>();


	public MCTSPolicySearchBuilder<N, A> withMDP(final IMDP<N, A, Double> mdp) {
		this.mdp = mdp;
		return this;
	}

	public MCTSPolicySearchBuilder<N, A> withGamma(final double gamma) {
		this.gamma = gamma;
		return this;
	}

	public MCTSPolicySearchBuilder<N, A> withMaxIterations(final int maxIterations) {
		this.maxIterations = maxIterations;
		return this;
	}

	public MCTSPolicySearch<N, A> build() {
		if (this.treePolicy == null) {
			this.treePolicy = new UCBPolicy<>(this.mdp.isMaximizing());
		}
		return new MCTSPolicySearch<>(this.mdp, this.treePolicy, this.defaultPolicy, this.maxIterations, this.gamma, this.epsilon);
	}
}
