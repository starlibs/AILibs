package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;

public class DNGTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> MCTSFactory<N, A> getFactory() {
		return DNG;
	}

}