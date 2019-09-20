package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class LocalImprovementWinComputer implements IWinComputer {

	private final int k = 30;
	private final int percentile = 10;
	private final Map<BTModel, DescriptiveStatistics> statsPerNode = new HashMap<>();
	private final Map<BTModel, Queue<Double>> latestObservationsNotConsideredInTheModel = new HashMap<>();

	/**
	 * Updates only the side for which a new result arrives. It asks whether the rollout is better than the known mean
	 */
	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean forRightChild) {
		BTModel updatedModel = forRightChild ? nodeModel.right : nodeModel.left;
		DescriptiveStatistics statsOfNode = this.statsPerNode.computeIfAbsent(updatedModel, nm -> new DescriptiveStatistics());
		Queue<Double> latestObservationInNode = this.latestObservationsNotConsideredInTheModel.computeIfAbsent(updatedModel, nm -> new LinkedList<>());
		latestObservationInNode.add(newScore);
		if (latestObservationInNode.size() > this.k) {
			statsOfNode.addValue(latestObservationInNode.poll());
		}

		final double threshold = statsOfNode.getN() > 0 ? statsOfNode.getMean() : Double.MAX_VALUE;
		int improvingRuns = (int)latestObservationInNode.stream().filter(v -> v < threshold).count();
		//		if (nodeModel.depth < 5) {
		//			System.out.println(nodeModel.depth + " (" + updatedModel.getVisits() + " visits): " +  improvingRuns + "/" + latestObservationInNode.size() + " with threshold " + threshold);
		//		}
		if (forRightChild) {
			updatedModel.setWinsRight(improvingRuns);
		}
		else {
			updatedModel.setWinsLeft(improvingRuns);
		}
		//		if (nodeModel.depth < 5) {
		//			System.out.println(nodeModel.depth + " (" + threshold + " <- " + quantile + "; " + statsOfNode.getMean() +") with " + trimmedMeanStats.getN() + " items: " + improvingRunsLeft + "/" + improvingRunsRight + " of  " + nodeModel.observedScoresLeft.size() + "/" + nodeModel.observedScoresRight.size());
		//		}
	}

}
