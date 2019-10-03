package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.MinMaxPriorityQueue;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class EstimatorDistanceBasedWinComputer implements IWinComputer {

	private final Map<BTModel, DescriptiveStatistics> statsPerNode = new HashMap<>();

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean forRightChild) {
		DescriptiveStatistics statsOfNode = this.statsPerNode.computeIfAbsent(nodeModel, nm -> new DescriptiveStatistics());
		statsOfNode.addValue(newScore);
		if (nodeModel.getLeft() == null || nodeModel.getRight() == null) {
			return;
		}
		if (nodeModel.getLeft().isGoalNode() || nodeModel.getRight().isGoalNode()) { // do not adjust wins for almost-leaf-nodes
			return;
		}
		if (!this.statsPerNode.containsKey(nodeModel.getLeft())) {
			throw new IllegalStateException("No entries for left successor of " + nodeModel.getNode() + ": " + nodeModel.getLeft().getNode());
		}
		if (!this.statsPerNode.containsKey(nodeModel.getRight())) {
			throw new IllegalStateException("No entries for right successor of " + nodeModel.getNode() + ": " + nodeModel.getRight().getNode());
		}
		double indexLeft = this.statsPerNode.get(nodeModel.getLeft()).getMean() - this.statsPerNode.get(nodeModel.getLeft()).getStandardDeviation();
		double indexRight = this.statsPerNode.get(nodeModel.getRight()).getMean() - this.statsPerNode.get(nodeModel.getRight()).getStandardDeviation();
		final int limit = 100;
		List<Double> candidatesLeft = ((MinMaxPriorityQueue<Double>)nodeModel.observedScoresLeft).stream().sorted((v1,v2) -> Double.compare(Math.abs(v1 - indexLeft), Math.abs(v2 - indexLeft))).limit(limit).collect(Collectors.toList());
		List<Double> candidatesRight = ((MinMaxPriorityQueue<Double>)nodeModel.observedScoresRight).stream().sorted((v1,v2) -> Double.compare(Math.abs(v1 - indexRight), Math.abs(v2 - indexRight))).limit(limit).collect(Collectors.toList());
		int winsLeft = 0;
		int winsRight = 0;
		for (double l : candidatesLeft) {
			for (double r : candidatesRight) {
				if (l <= r) {
					winsLeft ++;
				}
				if (r <= l) {
					winsRight ++;
				}
			}
		}
		nodeModel.setWinsLeft(winsLeft);
		nodeModel.setWinsRight(winsRight);
		//		System.out.println(nodeModel.depth + ": " + winsLeft + "/" + winsRight);
	}

}
