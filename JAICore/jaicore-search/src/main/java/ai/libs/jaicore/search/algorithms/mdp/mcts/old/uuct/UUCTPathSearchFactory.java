package ai.libs.jaicore.search.algorithms.mdp.mcts.old.uuct;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.mdp.mcts.old.MCTSPathSearchFactory;

public class UUCTPathSearchFactory<I extends IPathSearchWithPathEvaluationsInput<T, A, Double>, T, A> extends MCTSPathSearchFactory<I, T, A, Double> {
	private int seed;
	private IUCBUtilityFunction utility;

	public int getSeed() {
		return this.seed;
	}

	public void setSeed(final int seed) {
		this.seed = seed;
	}

	public IUCBUtilityFunction getUtility() {
		return this.utility;
	}

	public void setUtility(final IUCBUtilityFunction utility) {
		this.utility = utility;
	}

	@Override
	public UUCTPathSearch<I, T, A> getAlgorithm() {
		assert this.getEvaluationFailurePenalty() != null : "The evaluationFailurePenalty must not be null!";
		return new UUCTPathSearch<>(this.getInput(), this.utility, this.seed, this.getEvaluationFailurePenalty());
	}
}