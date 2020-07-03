package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSBuilder;
import ai.libs.jaicore.search.algorithms.mdp.mcts.brue.BRUEFactory;

public class BRUETester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> MCTSBuilder<N, A, ?> getFactory() {
		return new BRUEFactory<>();
	}
}