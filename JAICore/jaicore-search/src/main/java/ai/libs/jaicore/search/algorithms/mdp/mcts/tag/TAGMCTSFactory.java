package ai.libs.jaicore.search.algorithms.mdp.mcts.tag;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class TAGMCTSFactory<N, A> extends MCTSFactory<N, A, TAGMCTSFactory<N, A>> {

	private double explorationConstant = Math.sqrt(2);
	private int s = 100;
	private double delta = 0.01;
	private double thresholdIncrement = 0.001;

	public double getExplorationConstant() {
		return this.explorationConstant;
	}

	public void setExplorationConstant(final double explorationConstant) {
		this.explorationConstant = explorationConstant;
	}

	public int getS() {
		return this.s;
	}

	public void setS(final int s) {
		this.s = s;
	}

	public double getDelta() {
		return this.delta;
	}

	public void setDelta(final double delta) {
		this.delta = delta;
	}

	/**
	 * The DELTA in the Streeter paper
	 *
	 * @param thresholdIncrement
	 */
	public void setThresholdIncrement(final double thresholdIncrement) {
		this.thresholdIncrement = thresholdIncrement;
	}

	@Override
	public TAGMCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		return new TAGMCTS<>(input, this.explorationConstant, this.s, this.delta, this.thresholdIncrement, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), this.getRandom(), this.isTabooExhaustedNodes());
	}
}
