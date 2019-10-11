package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class BootstrappingWinComputer implements IWinComputer {

	private Map<BTModel, DescriptiveStatistics> observations = new HashMap<>();

	private final int maxNumSamples = 100;
	private final int numBootstraps = 100;
	private final Random random = new Random(0);

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean forRightChild) {

		this.observations.computeIfAbsent(nodeModel, nm -> new DescriptiveStatistics()).addValue(newScore);
		DescriptiveStatistics statsLeft = this.observations.get(nodeModel.getLeft());
		DescriptiveStatistics statsRight = this.observations.get(nodeModel.getRight());

		int winsLeft = 0;
		int winsRight = 0;
		if (statsLeft == null || statsRight == null) {
			return;
		}
		double[] valsLeft = statsLeft.getValues();
		double[] valsRight = statsRight.getValues();
		for (int bootstrap = 0; bootstrap < this.numBootstraps; bootstrap++) {
			DescriptiveStatistics subSampleLeft = new DescriptiveStatistics();
			DescriptiveStatistics subSampleRight = new DescriptiveStatistics();
			for (int sample = 0; sample < Math.min(statsLeft.getN(), this.maxNumSamples); sample++) {
				subSampleLeft.addValue(valsLeft[this.random.nextInt(valsLeft.length)]);
				subSampleRight.addValue(valsRight[this.random.nextInt(valsRight.length)]);
			}
			double valLeft = subSampleLeft.getMin();
			double valRight = subSampleRight.getMin();
			if (valLeft <= valRight) {
				winsLeft ++;
			}
			if (valRight <= valLeft) {
				winsRight ++;
			}
		}
		//		System.out.println(nodeModel.depth + ": " + winsLeft + "/" + winsRight);
		nodeModel.setWinsLeft(winsLeft);
		nodeModel.setWinsRight(winsRight);
	}
}
