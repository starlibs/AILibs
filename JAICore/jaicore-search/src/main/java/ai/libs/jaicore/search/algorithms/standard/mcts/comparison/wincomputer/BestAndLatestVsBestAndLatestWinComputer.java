package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MinMaxPriorityQueue;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class BestAndLatestVsBestAndLatestWinComputer implements IWinComputer {

	private Logger logger = LoggerFactory.getLogger(LatestVsLatestWinComputer.class);
	private final int kBest;
	private final int fBest = 0;
	private final int kLatest;

	private Map<BTModel, MinMaxPriorityQueue<Double>> bestValues = new HashMap<>();
	private Map<BTModel, Queue<Double>> latestValues = new HashMap<>();

	public BestAndLatestVsBestAndLatestWinComputer(final int kBest, final int kLatest) {
		super();
		this.kBest = kBest;
		this.kLatest = kLatest;
	}

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean forRightChild) {
		this.logger.debug("Recomputing wins for children of {}.", nodeModel);
		// System.out.println("Observing " + newScore + " for " + (forRightChild ? "right" : "left") + " child in depth " + nodeModel.depth+ ". Observed scores: " + nodeModel.observedScoresLeft + "; " + nodeModel.observedScoresRight);

		/* updating latest */
		Queue<Double> leftQueue = this.latestValues.computeIfAbsent(nodeModel.getLeft(), nm -> new LinkedList<>());
		Queue<Double> rightQueue = this.latestValues.computeIfAbsent(nodeModel.getRight(), nm -> new LinkedList<>());
		Queue<Double> updatedQueue = forRightChild ? rightQueue : leftQueue;
		updatedQueue.add(newScore);
		if (updatedQueue.size() > this.kLatest) {
			updatedQueue.poll();
		}

		/* updating best */
		MinMaxPriorityQueue<Double> leftBestQueue = this.bestValues.computeIfAbsent(nodeModel.getLeft(), nm -> MinMaxPriorityQueue.create());
		MinMaxPriorityQueue<Double> rightBestQueue = this.bestValues.computeIfAbsent(nodeModel.getRight(), nm -> MinMaxPriorityQueue.create());
		// System.out.println("PREV: " + leftBestQueue + "/" + rightBestQueue);
		MinMaxPriorityQueue<Double> updatedBestQueue = forRightChild ? rightBestQueue : leftBestQueue;
		updatedBestQueue.add(newScore);
		if (updatedBestQueue.size() > this.kBest) {
			updatedBestQueue.removeLast();
		}
		// System.out.println("NEXT: " + leftBestQueue + "/" + rightBestQueue);
		if (!leftBestQueue.isEmpty() && !leftBestQueue.peek().equals(nodeModel.observedScoresLeft.peek())) {
			throw new IllegalStateException("\nBest believed score left is " + leftBestQueue.peek() + ", which differs from true observed left optimum " + nodeModel.observedScoresLeft.peek());
		}
		if (!rightBestQueue.isEmpty() && !rightBestQueue.peek().equals(nodeModel.observedScoresRight.peek())) {
			throw new IllegalStateException("\nBest believed score right is " + rightBestQueue.peek() + ", which differs from true observed right optimum " + nodeModel.observedScoresRight.peek());
		}

		/* best clones */
		List<Double> leftClones = new ArrayList<>();
		List<Double> rightClones = new ArrayList<>();
		if (!leftBestQueue.isEmpty() && !rightBestQueue.isEmpty()) {
			if (leftBestQueue.peek() < rightBestQueue.peek()) {
				for (double best : leftBestQueue) {
					for (int i = 0; i < this.fBest; i++) {
						leftClones.add(best);
					}
				}
			} else if (rightBestQueue.peek() < leftBestQueue.peek()) {
				for (double best : rightBestQueue) {
					for (int i = 0; i < this.fBest; i++) {
						rightClones.add(best);
					}
				}
			}
		}

		List<Double> leftScores = new ArrayList<>();
		leftScores.addAll(leftQueue);
		leftScores.addAll(leftBestQueue);
		leftScores.addAll(leftClones);
		List<Double> rightScores = new ArrayList<>();
		rightScores.addAll(rightQueue);
		rightScores.addAll(rightBestQueue);
		rightScores.addAll(rightClones);

		int winsLeft = 0;
		int winsRight = 0;
		for (double leftScore : leftScores) {
			for (double rightScore : rightScores) {
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

		/* consistency check */
		if (!leftBestQueue.isEmpty() && !rightBestQueue.isEmpty()) {
			boolean substantialGap = Math.abs(leftBestQueue.peek() - rightBestQueue.peek()) > 0.05;
			if (substantialGap && leftBestQueue.peek() > rightBestQueue.peek() && winsLeft > winsRight) {
				System.err.println("inconsistency in depth " + nodeModel.depth + ": left has more wins than right! " + winsLeft + ", " + winsRight + "; " + leftScores + " vs " + rightScores);
			}
			if (substantialGap && leftBestQueue.peek() < rightBestQueue.peek() && winsLeft < winsRight) {
				System.err.println("inconsistency in depth " + nodeModel.depth + ": right has more wins than left! " + winsLeft + ", " + winsRight + "; " + leftScores + " vs " + rightScores);
			}
			// if (nodeModel.depth <= 5) {
			// System.out.println(nodeModel.depth + ": " + leftBestQueue.peek() + "/" + rightBestQueue.peek());
			// }
		}
	}

}
