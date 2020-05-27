package ai.libs.jaicore.search.algorithms.mdp.mcts.thompson;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class DNGMCTSFactory<N, A> extends MCTSFactory<N, A> {

	@Override
	public MCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		return new DNGMCTS<>(input, get, varianceFactor, initLambda);
	}
}
