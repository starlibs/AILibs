package ai.libs.jaicore.search.algorithms.mdp.mcts.brue;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class BRUEFactory<N, A> extends MCTSFactory<N, A, BRUEFactory<N, A>> {

	@Override
	public MCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		return new BRUE<>(input, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), this.getRandom(), this.isTabooExhaustedNodes());
	}
}
