package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import com.google.common.collect.MinMaxPriorityQueue;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class BestVsRestWinComputer implements IWinComputer {

	/**
	 * In this win model, we only let the best of each play against all the solutions of the other
	 *
	 * @param newScore
	 * @param scoreIsOfRightChild
	 */

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean scoreIsOfRightChild) {

		MinMaxPriorityQueue<Double> leftScores = nodeModel.observedScoresLeft;
		MinMaxPriorityQueue<Double> rightScores = nodeModel.observedScoresRight;
		int winsLeft = 0;
		int winsRight = 0;
		Double bestLeftScore = leftScores.peekFirst();
		if (bestLeftScore == null) {
			bestLeftScore = Double.MAX_VALUE;
		}
		Double bestRightScore = rightScores.peekFirst();
		if (bestRightScore == null) {
			bestRightScore = Double.MAX_VALUE;
		}

		/* now we duel the best left score against each score on the right */
		for (double rightScore : leftScores) {
			boolean winOfLeft = bestLeftScore <= rightScore;
			boolean winOfRight = rightScore <= bestLeftScore;
			if (winOfLeft) {
				winsLeft ++;
			}
			if (winOfRight) {
				winsRight ++;
			}
		}

		/* now we duel the best right score against all the scores on the left */
		for (double leftScore : leftScores) {
			boolean winOfLeft = leftScore <= bestRightScore;
			boolean winOfRight = bestRightScore <= leftScore;
			if (winOfLeft) {
				winsLeft ++;
			}
			if (winOfRight) {
				winsRight ++;
			}
		}
		nodeModel.setWinsLeft(winsLeft);
		nodeModel.setWinsRight(winsRight);
	}
}
