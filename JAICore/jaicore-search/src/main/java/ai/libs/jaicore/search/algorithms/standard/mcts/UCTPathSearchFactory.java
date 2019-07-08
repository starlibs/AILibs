package ai.libs.jaicore.search.algorithms.standard.mcts;

public class UCTPathSearchFactory<T, A> extends MCTSPathSearchFactory<T, A, Double> {
	private int seed;

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	@Override
	public UCTPathSearch<T, A> getAlgorithm() {
		assert getEvaluationFailurePenalty() != null : "The evaluationFailurePenalty must not be null!";
		return new UCTPathSearch<>(getInput(), seed, getEvaluationFailurePenalty(), isForbidDoublePaths());
	}
}
