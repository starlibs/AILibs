package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCTFactory;

public class UCTTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> UCTFactory<N, A> getFactory() {
		return new UCTFactory<>();
	}

}