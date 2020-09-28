package ai.libs.jaicore.search.algorithms.mdp.mcts.thompson;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class DNGMCTSFactory<N, A> extends MCTSFactory<N, A, DNGMCTSFactory<N, A>> {

	private double varianceFactor = 0;
	private double initLambda = 1.0;

	public double getVarianceFactor() {
		return this.varianceFactor;
	}

	public void setVarianceFactor(final double varianceFactor) {
		this.varianceFactor = varianceFactor;
	}

	public double getInitLambda() {
		return this.initLambda;
	}

	public void setInitLambda(final double initLambda) {
		this.initLambda = initLambda;
	}

	@Override
	public MCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		return new DNGMCTS<>(input, this.varianceFactor, this.initLambda, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), this.getRandom(), this.isTabooExhaustedNodes(), this.isMaximize());
	}
}
