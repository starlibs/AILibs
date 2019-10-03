package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class LatestVsLatestWinComputer implements IWinComputer {

	private Logger logger = LoggerFactory.getLogger(LatestVsLatestWinComputer.class);
	private final int k;
	private Map<BTModel, Queue<Double>> latestValues = new HashMap<>();

	public LatestVsLatestWinComputer(final int k) {
		super();
		this.k = k;
	}

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean scoreIsOfRightChild) {
		this.logger.debug("Recomputing wins for children of {}.", nodeModel);
		Queue<Double> leftQueue = this.latestValues.computeIfAbsent(nodeModel.left, nm -> new LinkedList<>());
		Queue<Double> rightQueue = this.latestValues.computeIfAbsent(nodeModel.right, nm -> new LinkedList<>());
		Queue<Double> updatedQueue = scoreIsOfRightChild ? rightQueue : leftQueue;
		updatedQueue.add(newScore);
		if (updatedQueue.size() > this.k) {
			updatedQueue.poll();
		}

		int winsLeft = 0;
		int winsRight = 0;
		for (double leftScore : leftQueue) {
			for (double rightScore : rightQueue) {
				boolean winOfLeft = leftScore <= rightScore;
				boolean winOfRight = rightScore <= leftScore;
				this.logger.trace("Comparing scores {} and {} implies win for left: {}; win for right: {}", leftScore, rightScore, winOfLeft, winOfRight);
				if (winOfLeft) {
					winsLeft++;
				}
				if (winOfRight) {
					winsRight++;
				}
			}
		}
		nodeModel.setWinsLeft(winsLeft);
		nodeModel.setWinsRight(winsRight);
	}
	// private void recomputeWins() {

	// }

}
