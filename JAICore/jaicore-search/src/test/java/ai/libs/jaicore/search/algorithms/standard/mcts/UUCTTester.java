package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uuct.UUCTFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uuct.utility.CVaR;

public class UUCTTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> MCTSFactory<N, A> getFactory() {
		UUCTFactory<N, A> factory = new UUCTFactory<>();
		factory.setUtility(new CVaR(0.05)); // test with conditional value at risk
		return factory;
	}
}