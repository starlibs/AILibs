package ai.libs.jaicore.search.algorithms.mdp.mcts.uuct;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class UUCTFactory<N, A> extends MCTSFactory<N, A> {

	private IUCBUtilityFunction utility;

	public IUCBUtilityFunction getUtility() {
		return this.utility;
	}

	public void setUtility(final IUCBUtilityFunction utility) {
		this.utility = utility;
	}

	@Override
	public MCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		return new UUCT<>(input, this.utility, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), this.getRandom(), this.isTabooExhaustedNodes());
	}

}
