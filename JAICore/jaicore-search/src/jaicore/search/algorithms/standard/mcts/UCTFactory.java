package jaicore.search.algorithms.standard.mcts;

public class UCTFactory<T, A> extends MCTSFactory<T, A, Double> {
	private int seed;

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	@Override
	public UCT<T, A> getAlgorithm() {
		return new UCT<>(getProblemInput(), seed);
	}
}
