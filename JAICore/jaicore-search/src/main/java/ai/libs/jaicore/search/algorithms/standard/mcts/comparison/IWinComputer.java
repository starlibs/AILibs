package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;

public interface IWinComputer {
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, double newScore, boolean forRightChild);
}
