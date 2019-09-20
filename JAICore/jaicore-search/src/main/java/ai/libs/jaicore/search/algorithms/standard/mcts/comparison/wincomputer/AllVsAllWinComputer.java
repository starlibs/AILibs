package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MinMaxPriorityQueue;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class AllVsAllWinComputer implements IWinComputer {

	private Logger logger = LoggerFactory.getLogger(AllVsAllWinComputer.class);

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean scoreIsOfRightChild) {
		MinMaxPriorityQueue<Double> scoresToCompareWith = scoreIsOfRightChild ? nodeModel.observedScoresLeft : nodeModel.observedScoresRight;
		int n = scoresToCompareWith.size();
		int winsLeft = nodeModel.getWinsLeft();
		int winsRight = nodeModel.getWinsRight();
		this.logger.debug("Updating wins for children of {} with new score {} arriving for {} child. There are {} scores of the other child to compare against.", nodeModel, newScore, scoreIsOfRightChild ? "right" : "left", n);
		for (double otherScore : scoresToCompareWith) {
			boolean winOfLeft = scoreIsOfRightChild && otherScore <= newScore || !scoreIsOfRightChild && otherScore >= newScore;
			boolean winOfRight = scoreIsOfRightChild && otherScore >= newScore || !scoreIsOfRightChild && otherScore <= newScore;
			this.logger.trace("Comparing scores {} and {} implies win for left: {}; win for right: {}", newScore, otherScore, winOfLeft, winOfRight);
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
	//	private void recomputeWins() {
	//		BradleyTerryLikelihoodPolicy.this.logger.debug("Recomputing wins for children of {}.",  this.node);
	//		this.winsLeft = 0;
	//		this.winsRight = 0;
	//		for (double leftScore : this.observedScoresLeft) {
	//			for (double rightScore : this.observedScoresRight) {
	//				boolean winOfLeft = leftScore <= rightScore;
	//				boolean winOfRight = rightScore <= leftScore;
	//				BradleyTerryLikelihoodPolicy.this.logger.trace("Comparing scores {} and {} implies win for left: {}; win for right: {}", leftScore, rightScore, winOfLeft, winOfRight);
	//				if (winOfLeft) {
	//					this.winsLeft ++;
	//				}
	//				if (winOfRight) {
	//					this.winsRight ++;
	//				}
	//			}
	//		}
	//	}

}
