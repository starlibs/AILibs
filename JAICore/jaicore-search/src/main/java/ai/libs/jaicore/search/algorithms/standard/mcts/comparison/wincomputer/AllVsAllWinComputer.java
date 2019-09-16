package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class AllVsAllWinComputer implements IWinComputer {

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean scoreIsOfRightChild) {

		//		private void recomputeWins() {
		//			BradleyTerryLikelihoodPolicy.this.logger.debug("Recomputing wins for children of {}.",  this.node);
		//			this.winsLeft = 0;
		//			this.winsRight = 0;
		//			for (double leftScore : this.observedScoresLeft) {
		//				for (double rightScore : this.observedScoresRight) {
		//					boolean winOfLeft = leftScore <= rightScore;
		//					boolean winOfRight = rightScore <= leftScore;
		//					BradleyTerryLikelihoodPolicy.this.logger.trace("Comparing scores {} and {} implies win for left: {}; win for right: {}", leftScore, rightScore, winOfLeft, winOfRight);
		//					if (winOfLeft) {
		//						this.winsLeft ++;
		//					}
		//					if (winOfRight) {
		//						this.winsRight ++;
		//					}
		//				}
		//			}
		//		}
		//
		//		private void updateWins(final double newScore, final boolean scoreIsOfRightChild) {
		//			MinMaxPriorityQueue<Double> scoresToCompareWith = scoreIsOfRightChild ? this.observedScoresLeft : this.observedScoresRight;
		//			int n = scoresToCompareWith.size();
		//			BradleyTerryLikelihoodPolicy.this.logger.debug("Updating wins for children of {} with new score {} arriving for {} child. There are {} scores of the other child to compare against.", this.node, newScore, scoreIsOfRightChild ? "right" : "left", n);
		//			for (double otherScore : scoresToCompareWith) {
		//				boolean winOfLeft = scoreIsOfRightChild && otherScore <= newScore || !scoreIsOfRightChild && otherScore >= newScore;
		//				boolean winOfRight = scoreIsOfRightChild && otherScore >= newScore || !scoreIsOfRightChild && otherScore <= newScore;
		//				BradleyTerryLikelihoodPolicy.this.logger.trace("Comparing scores {} and {} implies win for left: {}; win for right: {}", newScore, otherScore, winOfLeft, winOfRight);
		//				if (winOfLeft) {
		//					this.winsLeft ++;
		//				}
		//				if (winOfRight) {
		//					this.winsRight ++;
		//				}
		//			}
		//		}
	}

}
