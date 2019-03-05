package jaicore.ml.tsc.classifier.trees;

import org.junit.Assert;
import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;

/**
 * Unit tests of the TSBF algorithm class.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesBagOfFeaturesAlgorithmTest {
	@Test
	public void discretizeProbsTest() {
		int numBins = 10;
		double[][] probs = new double[][] { { 0.19, 0.21, 0.25 } };

		Assert.assertArrayEquals(new int[] { 1, 2, 2 },
				TimeSeriesBagOfFeaturesAlgorithm.discretizeProbs(numBins, probs)[0]);
	}

	@Test
	public void formHistogramsAndRelativeFreqsTest() {
		int[][] discretizedProbs = new int[][] { { 1, 2, 3 } };
		int numBins = 5;
		int[] targets = new int[] {1};
		int numInstances = 1;
		int numClasses = 3;
		
		Pair<int[][][], int[][]> result = TimeSeriesBagOfFeaturesAlgorithm
				.formHistogramsAndRelativeFreqs(discretizedProbs, targets, numInstances, numClasses, numBins);
		int[][][] histograms = result.getX();
		int[][] relativeFreqs = result.getY();

		// Check histograms
		Assert.assertArrayEquals(new int[] { 0, 1, 0, 0, 0 }, histograms[0][0]);
		Assert.assertArrayEquals(new int[] { 0, 0, 1, 0, 0 }, histograms[0][1]);
		Assert.assertArrayEquals(new int[] { 0, 0, 0, 1, 0 }, histograms[0][2]);

		// Check relative freqs
		Assert.assertArrayEquals(new int[] { 0, 0, 1 }, relativeFreqs[0]);

	}

	@Test
	public void generateHistogramInstancesTest() {

	}
}
