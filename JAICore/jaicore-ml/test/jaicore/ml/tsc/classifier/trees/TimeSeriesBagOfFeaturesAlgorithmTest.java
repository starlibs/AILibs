package jaicore.ml.tsc.classifier.trees;

import org.junit.Assert;
import org.junit.Before;
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

	private TimeSeriesBagOfFeaturesAlgorithm algorithm;

	/**
	 * Hyperparameters
	 */
	private static final int SEED = 42;
	private static final int NUM_BINS = 10;
	private static final int NUM_FOLDS = 10;
	private static final double Z_PROP = 0.1;
	private static final int MIN_INTERVAL_LENGTH = 5;
	private static final boolean USE_Z_NORMALIZATION = false;

	/**
	 * Setting up objects used within the tests.
	 */
	@Before
	public void setup() {
		algorithm = new TimeSeriesBagOfFeaturesAlgorithm(SEED, NUM_BINS, NUM_FOLDS, Z_PROP, MIN_INTERVAL_LENGTH,
				USE_Z_NORMALIZATION);
	}

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#discretizeProbs(int, double[][])}.
	 */
	@Test
	public void discretizeProbsTest() {
		double[][] probs = new double[][] { { 0.19, 0.21, 0.25 } };

		Assert.assertArrayEquals("The calculated discretized probabilites do not match the expected results.",
				new int[] { 1, 2, 2 },
				TimeSeriesBagOfFeaturesAlgorithm.discretizeProbs(NUM_BINS, probs)[0]);
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
		Assert.assertArrayEquals("The calculated histograms at first place do not match the expected results.",
				new int[] { 0, 1, 0, 0, 0 }, histograms[0][0]);
		Assert.assertArrayEquals("The calculated histograms at second place do not match the expected results.",
				new int[] { 0, 0, 1, 0, 0 }, histograms[0][1]);

		// Check relative freqs
		Assert.assertArrayEquals("The calculated relative class frequencies do not match the expected results.",
				new int[] { 0, 0, 1 }, relativeFreqs[0]);

	}

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#generateHistogramInstances(int[][][], int[][])}.
	 */
	@Test
	public void generateHistogramInstancesTest() {
		int[][][] histograms = new int[][][] { { { 1, 0, 1 }, { 0, 1, 1 } } };
		int[][] relativeFreqsOfClasses = new int[][] { { 0, 2 } };

		Assert.assertArrayEquals("The calculated histogram instances do not match the expected results.",
				new double[] { 1, 0, 1, 0, 1, 1, 0, 2 },
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
		Assert.assertArrayEquals("The generated features do not match the expected results.",
				new double[] { 1.5, 0.25, 1 }, generatedFeatures[0][0][0], EPS);

		// Features of the first interval (last instance)
		Assert.assertArrayEquals("The generated features do not match the expected results.", new double[] { 0, 0, 0 },
				generatedFeatures[1][0][0], EPS);

		// Features of the subsequence (first instance)
		Assert.assertArrayEquals("The generated features do not match the expected results.",
				new double[] { 2, 0.66667, 1 }, generatedFeatures[0][0][1], EPS);

		// Features of the subsequence (last instance)
		Assert.assertArrayEquals("The generated features do not match the expected results.", new double[] { 0, 0, 0 },
				generatedFeatures[1][0][1], EPS);
	}

	/**
	 * See
	 * {@link TimeSeriesBagOfFeaturesAlgorithm#generateSubsequencesAndIntervals(int, int, int, int)}.
	 */
	@Test
	public void generateSubsequencesAndIntervalsTest() {

		final int r = 2;
		final int d = 1;
		final int T = 100;
		final int lMin = 2;

		final Pair<int[][], int[][][]> subsequencesAndIntervals = algorithm.generateSubsequencesAndIntervals(r, d, lMin,
				T);
		final int[][] subsequences = subsequencesAndIntervals.getX();
		final int[][][] intervals = subsequencesAndIntervals.getY();

		// Check subsequences dimensionality
		Assert.assertEquals("The dimensionality of the calculated subsequences does not match the expected result.",
				r - d, subsequences.length);
		Assert.assertEquals(
				"The dimensionality of the calculated subsequence indices does not match the expected result.", 2,
				subsequences[0].length);

		// Check intervals dimensionality
		Assert.assertEquals("The dimensionality of the calculated intervals does not match the expected result.",
				r - d, intervals.length);
		Assert.assertEquals(
				"The dimensionality of the calculated intervals per subsequence does not match the expected result.", d,
				intervals[0].length);
		
		// Check interval values
		Assert.assertTrue("The range of the first intervals is not greater equals than the interval minimum length.",
				intervals[0][0][1] - intervals[0][0][0] >= MIN_INTERVAL_LENGTH);
		Assert.assertTrue(
				"The range of the first subsequence is not greater equals than the minimum subsequence length.",
				subsequences[0][1] - subsequences[0][0] >= lMin);
	}
}
