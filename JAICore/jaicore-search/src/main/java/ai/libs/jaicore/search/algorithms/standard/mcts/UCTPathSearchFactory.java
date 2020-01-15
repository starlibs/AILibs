package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

public class UCTPathSearchFactory<I extends IPathSearchWithPathEvaluationsInput<T, A, Double>, T, A> extends MCTSPathSearchFactory<I, T, A, Double> {
	private int seed;

	public int getSeed() {
		return this.seed;
	}

	public void setSeed(final int seed) {
		this.seed = seed;
	}

	@Override
	public UCTPathSearch<I, T, A> getAlgorithm() {
		assert this.getEvaluationFailurePenalty() != null : "The evaluationFailurePenalty must not be null!";
		return new UCTPathSearch<>(this.getInput(), Math.sqrt(2), this.seed, this.getEvaluationFailurePenalty());
	}
}