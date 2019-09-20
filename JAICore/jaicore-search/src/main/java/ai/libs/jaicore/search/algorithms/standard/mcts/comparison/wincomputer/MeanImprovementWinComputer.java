package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class MeanImprovementWinComputer implements IWinComputer {

	private final Map<BTModel, DescriptiveStatistics> statsPerNode = new HashMap<>();

	/**
	 * Updates only the side for which a new result arrives. It asks whether the rollout is better than the known mean
	 */
	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean forRightChild) {
		DescriptiveStatistics statsOfNode = this.statsPerNode.computeIfAbsent(nodeModel, nm -> new DescriptiveStatistics());
		statsOfNode.addValue(newScore);
		final double quantile = statsOfNode.getPercentile(10);
		DescriptiveStatistics trimmedMeanStats = new DescriptiveStatistics();
		for (double d : statsOfNode.getSortedValues()) {
			if (d > quantile) {
				break;
			}
			trimmedMeanStats.addValue(d);
		}
		final double threshold = trimmedMeanStats.getMean();
		long improvingRunsLeft = nodeModel.observedScoresLeft.stream().filter(v -> (double)v < threshold).count();
		long improvingRunsRight = nodeModel.observedScoresRight.stream().filter(v -> (double)v < threshold).count();
		nodeModel.setWinsRight((int)improvingRunsRight);
		nodeModel.setWinsLeft((int)improvingRunsLeft);
		//		if (nodeModel.depth < 5) {
		//			System.out.println(nodeModel.depth + " (" + threshold + " <- " + quantile + "; " + statsOfNode.getMean() +") with " + trimmedMeanStats.getN() + " items: " + improvingRunsLeft + "/" + improvingRunsRight + " of  " + nodeModel.observedScoresLeft.size() + "/" + nodeModel.observedScoresRight.size());
		//		}
	}

}
