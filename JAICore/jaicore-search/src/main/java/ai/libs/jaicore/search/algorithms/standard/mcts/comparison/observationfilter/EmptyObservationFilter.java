package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.observationfilter;

import com.google.common.collect.MinMaxPriorityQueue;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IObservationUpdate;

public class EmptyObservationFilter implements IObservationUpdate {

	@Override
	public boolean updateObservations(final BTModel model, final double score, final boolean isForRightChild) {
		MinMaxPriorityQueue<Double> queue = isForRightChild ? model.observedScoresRight : model.observedScoresLeft;
		queue.add(score);
		return false;
	}

}
