package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.spuct.SPUCTFactory;

public class SPUCTTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> MCTSFactory<N, A> getFactory() {
		return new SPUCTFactory<>();
	}
}