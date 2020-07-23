package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.thompson.DNGMCTSFactory;

public class DNGTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> DNGMCTSFactory<N, A> getFactory() {
		return new DNGMCTSFactory<>();
	}

}