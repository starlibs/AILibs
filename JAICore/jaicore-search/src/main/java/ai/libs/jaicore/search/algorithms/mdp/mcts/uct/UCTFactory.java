package ai.libs.jaicore.search.algorithms.mdp.mcts.uct;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSBuilder;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class UCTFactory<N, A> extends MCTSBuilder<N, A, UCTFactory<N, A>> {

	@Override
	public MCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		return new UCT<>(input, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), this.getRandom(), this.isTabooExhaustedNodes());
	}
}
