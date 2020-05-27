package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.tag.TAGMCTSFactory;

public class TAGTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> MCTSFactory<N, A> getFactory() {
		return new TAGMCTSFactory<>();
	}
}