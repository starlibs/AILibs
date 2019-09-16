package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;

public interface IObservationUpdate {
	public boolean updateObservations(BTModel model, double score, boolean isForRightChild); // returns true if observations were REMOVED
}
