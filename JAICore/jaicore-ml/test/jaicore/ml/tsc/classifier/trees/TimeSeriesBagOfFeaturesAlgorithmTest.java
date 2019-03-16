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

	/**
	 * Maximal delta for asserts with precision.
	 */
	private static final double EPS = 0.00001;

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#discretizeProbs(int, double[][])}.
	 */
	@Test
	public void discretizeProbsTest() {
		int numBins = 10;
		double[][] probs = new double[][] { { 0.19, 0.21, 0.25 } };

		Assert.assertArrayEquals(new int[] { 1, 2, 2 },
				TimeSeriesBagOfFeaturesAlgorithm.discretizeProbs(numBins, probs)[0]);
	}

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#formHistogramsAndRelativeFreqs(int[][], int[], int, int, int)}.
	 */
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

		// Check relative freqs
		Assert.assertArrayEquals(new int[] { 0, 0, 1 }, relativeFreqs[0]);

	}

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#generateHistogramInstances(int[][][], int[][])}.
	 */
	@Test
	public void generateHistogramInstancesTest() {
		int[][][] histograms = new int[][][] { { { 1, 0, 1 }, { 0, 1, 1 } } };
		int[][] relativeFreqsOfClasses = new int[][] { { 0, 2 } };

		Assert.assertArrayEquals(new double[] { 1, 0, 1, 0, 1, 1, 0, 2 },
				TimeSeriesBagOfFeaturesAlgorithm.generateHistogramInstances(histograms, relativeFreqsOfClasses)[0],
				EPS);
	}

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#generateFeatures(double[][], int, int, int[][], int[][][])}.
	 */
	@Test
	public void generateFeaturesTest() {
		final double[][] data = new double[][] { { 1, 2, 3, 4 }, { 0, 0, 0, 0 } };
		final int[][] subsequences = new int[][] { { 0, 3 } };
		final int[][][] intervals = new int[][][] { { { 0, 2 } } };

		double[][][][] generatedFeatures = TimeSeriesBagOfFeaturesAlgorithm.generateFeatures(data, subsequences,
				intervals);

		// Features of the first interval (first instance)
		Assert.assertArrayEquals(new double[] { 1.5, 0.25, 1 }, generatedFeatures[0][0][0], EPS);

		// Features of the first interval (last instance)
		Assert.assertArrayEquals(new double[] { 0, 0, 0 }, generatedFeatures[1][0][0], EPS);

		// Features of the subsequence (first instance)
		Assert.assertArrayEquals(new double[] { 2, 0.66667, 1 }, generatedFeatures[0][0][1], EPS);

		// Features of the subsequence (last instance)
		Assert.assertArrayEquals(new double[] { 0, 0, 0 }, generatedFeatures[1][0][1], EPS);
	}

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#generateSubsequencesAndIntervals(int, int, int, int)}.
	 */
	@Test
	public void generateSubsequencesAndIntervalsTest() {
		final int minIntervalLength = 5;
		
		final TimeSeriesBagOfFeaturesAlgorithm algorithm = new TimeSeriesBagOfFeaturesAlgorithm(42, 10, 10, 0.1, minIntervalLength,
				false);

		final int r = 2;
		final int d = 1;
		final int T = 100;
		final int lMin = 2;

		final Pair<int[][], int[][][]> subsequencesAndIntervals = algorithm.generateSubsequencesAndIntervals(r, d, lMin,
				T);
		final int[][] subsequences = subsequencesAndIntervals.getX();
		final int[][][] intervals = subsequencesAndIntervals.getY();

		// Check subsequences dimensionality
		Assert.assertEquals(r - d, subsequences.length);
		Assert.assertEquals(2, subsequences[0].length);

		// Check intervals dimensionality
		Assert.assertEquals(r - d, intervals.length);
		Assert.assertEquals(d, intervals[0].length);
		
		// Check interval values
		Assert.assertTrue(intervals[0][0][1] - intervals[0][0][0] > minIntervalLength);
		Assert.assertTrue(subsequences[0][1] - subsequences[0][0] > lMin);
	}
}
