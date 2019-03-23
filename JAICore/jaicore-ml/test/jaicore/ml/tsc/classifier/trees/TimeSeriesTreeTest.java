package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.TreeNode;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.trees.TimeSeriesTree.TimeSeriesTreeNodeDecisionFunction;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.features.TimeSeriesFeature;
import jaicore.ml.tsc.features.TimeSeriesFeature.FeatureType;
import junit.framework.Assert;

/**
 * Unit tests for the time series tree classifier.
 * 
 * @author Julian Lienen
 *
 */
@SuppressWarnings("unused")
public class TimeSeriesTreeTest {

	/**
	 * Maximal delta for asserts with precision.
	 */
	private static final double EPS_DELTA = 0.000001;

	/**
	 * Log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesTreeTest.class);
	
	/**
	 * Algorithm object used within the tests.
	 */
	private TimeSeriesTreeAlgorithm algorithm;
	
	/**
	 * Hyperparameters
	 */
	public static final int SEED = 42;
	public static final int MAX_DEPTH = 10;

	/**
	 * Setting up objects used within the tests.
	 */
	@Before
	public void setup() {
		algorithm = new TimeSeriesTreeAlgorithm(MAX_DEPTH, SEED);
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#calculateFeature(FeatureType, double[], int, int, boolean)}.
	 */
	@Test
	public void calculateFeatureTest() {
		double[] instance = new double[] { 1, 2, 3 };
		// Mean
		Assert.assertEquals("The calculated feature does not match the expected feature value.", 2d, TimeSeriesFeature
				.calculateFeature(FeatureType.MEAN, instance, 0, 2,
				TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION), EPS_DELTA);
		Assert.assertEquals("The calculated feature does not match the expected feature value.", 1.5d,
				TimeSeriesFeature.calculateFeature(FeatureType.MEAN, instance, 0, 1,
						TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION),
				EPS_DELTA);
		// Standard deviation
		Assert.assertEquals("The calculated feature does not match the expected feature value.", 1d,
				TimeSeriesFeature.calculateFeature(FeatureType.STDDEV, instance, 0, 2,
						TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION),
				EPS_DELTA);
		// Slope
		Assert.assertEquals("The calculated feature does not match the expected feature value.", 1d, TimeSeriesFeature
				.calculateFeature(FeatureType.SLOPE, instance, 0, 2,
				TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION), EPS_DELTA);

		double[] features = TimeSeriesFeature.getFeatures(instance, 0, 2,
				TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION);
		Assert.assertEquals("The calculated feature does not match the expected feature value.", 2d, features[0],
				EPS_DELTA);
		Assert.assertEquals("The calculated feature does not match the expected feature value.", 1d, features[1],
				EPS_DELTA);
		Assert.assertEquals("The calculated feature does not match the expected feature value.", 1d, features[2],
				EPS_DELTA);
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#randomlySampleNoReplacement(List, int, int)}.
	 */
	@Test
	public void randomlySampleNoReplacementTest() {
		int m = 40;
		List<Integer> sampleBase = IntStream.range(0, 100).boxed().collect(Collectors.toList());

		List<Integer> samples = TimeSeriesTreeAlgorithm.randomlySampleNoReplacement(sampleBase, m, SEED);
		Assert.assertEquals(
				"The number of randomly sampled values without replacement does not match the expected number of samples.",
				m, samples.size());
	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#sampleIntervals(int, int)}.
	 */
	@Test
	public void sampleIntervalsTest() {
		int m = 40;

		Pair<List<Integer>, List<Integer>> result = TimeSeriesTreeAlgorithm.sampleIntervals(m, SEED);

		Assert.assertEquals("The number of generated start indices does not match the number of generated end indices.",
				result.getX().size(), result.getY().size());
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#generateThresholdCandidates(Pair, int, double[][][])}.
	 */
	@Test
	public void generateThresholdCandidatesTest() {
		List<Integer> T1 = Arrays.asList(0, 1);
		List<Integer> T2 = Arrays.asList(2, 3);
		int numOfCandidates = 4;
		Pair<List<Integer>, List<Integer>> T1T2 = new Pair<>(T1, T2);
		double[][][] transformedInstances = new double[][][] { { { 3 }, { 0 } }, { { 5 }, { 2 } }, { { 2 }, { 0 } } };
		
		List<List<Double>> thresholdCandidates = TimeSeriesTreeAlgorithm.generateThresholdCandidates(T1T2,
				numOfCandidates, transformedInstances);

		Assert.assertEquals("The number of generated threshold candidates do not match the expected number.", 3,
				thresholdCandidates.size());
		Assert.assertEquals("The number of generated threshold candidates do not match the expected number.",
				numOfCandidates, thresholdCandidates.get(0).size());
		Assert.assertEquals("The number of generated threshold candidates do not match the expected number.",
				numOfCandidates, thresholdCandidates.get(1).size());
		Assert.assertEquals("The number of generated threshold candidates do not match the expected number.",
				numOfCandidates, thresholdCandidates.get(2).size());

		Assert.assertEquals("The generated threshold candidate does not match the expected candidate.", 6d / 5d,
				thresholdCandidates.get(0).get(1), EPS_DELTA);
		Assert.assertEquals("The generated threshold candidate does not match the expected candidate.", 6d / 5d + 2d,
				thresholdCandidates.get(1).get(1), EPS_DELTA);
		Assert.assertEquals("The generated threshold candidate does not match the expected candidate.", 4d / 5d,
				thresholdCandidates.get(2).get(1), EPS_DELTA);
	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#transformInstances(double[][], Pair)}.
	 */
	@Test
	public void transformInstancesTest() {
		double[][] data = new double[][] { { 0, 1, 2, 3, 4, 5, 6 }, { 2, 4, 6, 8, 10, 12, 14 } };
		List<Integer> T1 = Arrays.asList(0, 0);
		List<Integer> T2 = Arrays.asList(1, 2);
		Pair<List<Integer>, List<Integer>> T1T2 = new Pair<>(T1, T2);

		double[][][] transformedData = algorithm.transformInstances(data, T1T2);

		Assert.assertEquals("The number of generated feature types does not match the expected number of types.", 3,
				transformedData.length);
		Assert.assertEquals(
				"The number of interval pairs for the first feature type does not match the number of start indices.",
				T1.size(), transformedData[0].length);
		Assert.assertEquals(
				"The number of interval pairs for the first feature type does not match the number of end indices.",
				T2.size(), transformedData[0].length);
		Assert.assertEquals(
				"The number of instances for the first feature type does not match the number of total instances.",
				data.length, transformedData[0][0].length);

		Assert.assertEquals(
				"The mean of the first two elements of the first instance does not match the expected mean.", 0.5d,
				transformedData[0][0][0], EPS_DELTA); // Mean of first two elements of first
																			// instance
		Assert.assertEquals(
				"The mean of the first three elements of the first instance does not match the expected mean.", 1d,
				transformedData[0][1][0], EPS_DELTA); // Mean of first three elements of first
																			// instance

		Assert.assertEquals(
				"The stddev of the first two elements of the first instance does not match the expected mean.",
				Math.sqrt(0.5d), transformedData[1][0][0], EPS_DELTA); // Stddev of first two elements of
																						// first
																			// instance
		Assert.assertEquals(
				"The stddev of the first three elements of the first instance does not match the expected mean.",
				Math.sqrt(1d), transformedData[1][1][0], EPS_DELTA); // Stddev of first three elements of
																					// first
																			// instance

		Assert.assertEquals(
				"The slope of the first two elements of the first instance does not match the expected mean.", 1d,
				transformedData[2][0][0], EPS_DELTA); // Slope of first two elements of
																						// first instance
		Assert.assertEquals(
				"The slope of the three two elements of the second instance does not match the expected mean.", 2d,
				transformedData[2][1][1], EPS_DELTA); // Slope of first three elements of
																			// second instance
	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#calculateMargin(double[], double)}.
	 */
	@Test
	public void calculateMarginTest() {
		double[] dataValues = new double[] { 0, 1, 2, 3, 4, 5 };
		double thresholdCandidate = 1.5d;
		Assert.assertEquals("The calculated margin does not match the expected margin.", 0.5d,
				TimeSeriesTreeAlgorithm.calculateMargin(dataValues, thresholdCandidate), EPS_DELTA);

		dataValues = new double[] { 2, 4, 6, 7 };
		thresholdCandidate = 0d;
		Assert.assertEquals("The calculated margin does not match the expected margin.", 2d,
				TimeSeriesTreeAlgorithm.calculateMargin(dataValues, thresholdCandidate), EPS_DELTA);
	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#calculateEntrance(double, double)}.
	 */
	@Test
	public void calculateEntranceTest() {
		double[] dataValues = new double[] { 0, 0, 1, 1, 2, 2, 3, 3 };
		int[] targets = new int[] { 0, 0, 0, 0, 0, 0, 1, 1 };
		double thresholdCandidate = 2;
		List<Integer> classes = Arrays.asList(0, 1);
		double parentEntropy = 1d;

		double deltaEntropy = TimeSeriesTreeAlgorithm
				.calculateDeltaEntropy(dataValues, targets, thresholdCandidate, classes, parentEntropy);
		double margin = TimeSeriesTreeAlgorithm.calculateMargin(dataValues, thresholdCandidate);
		
		Assert.assertEquals("The calculcated entrance does not match the expected value.",
				1d - TimeSeriesTreeAlgorithm.ENTROPY_APLHA * 0,
				TimeSeriesTreeAlgorithm.calculateEntrance(deltaEntropy, margin));
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#calculateDeltaEntropy(double[], int[], double, List, double)}.
	 */
	@Test
	public void calculateDeltaEntropyTest() {
		double[] dataValues = new double[] { 0, 0, 1, 1, 2, 2, 3, 3 };
		int[] targets = new int[] { 0, 0, 0, 0, 0, 0, 1, 1 };
		double thresholdCandidate = 2;
		List<Integer> classes = Arrays.asList(0, 1);
		double parentEntropy = 1d;

		Assert.assertEquals("The calculcated delta entropy does not match the expected value.",
				parentEntropy + 6d / 8d * Math.log(1d) + 2d / 8d * 0d, TimeSeriesTreeAlgorithm
				.calculateDeltaEntropy(dataValues, targets, thresholdCandidate, classes, parentEntropy), EPS_DELTA);

	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#getBestSplitIndex(double[])}.
	 */
	@Test
	public void getBestSplitIndexTest() {
		
		double[] deltaEntropyStarPerFeatureType = new double[] { 1, 6, 7 };
		Assert.assertEquals("The induced best split index does not match the expected index.", 2,
				algorithm.getBestSplitIndex(deltaEntropyStarPerFeatureType));

		deltaEntropyStarPerFeatureType = new double[] { 2, 0.01, -1 };
		Assert.assertEquals("The induced best split index does not match the expected index.", 0,
				algorithm.getBestSplitIndex(deltaEntropyStarPerFeatureType));
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#getChildDataIndices(double[][][], int, int, int, double)}.
	 */
	@Test
	public void getChildDataIndicesTest() {
		double[][][] transformedFeatures = new double[][][] { { { 0, 1.2d }, { 1, 6d } }, { { 3, 1.1d }, { 2, 0.5d } },
				{ { 1, 1.34d }, { 0, 3.2d } } };
		int n = 2;
		int k = 1;
		int t1t2 = 0;
		double threshold = 1.1d;

		Pair<List<Integer>, List<Integer>> childDataIndices = TimeSeriesTreeAlgorithm
				.getChildDataIndices(transformedFeatures, n, k, t1t2, threshold);

		Assert.assertEquals(
				"The number of instances assigned to both children does not match the expected number of instances.", n,
				childDataIndices.getX().size() + childDataIndices.getY().size());
		Assert.assertEquals(
				"The number of instances assigned to the left child does not match the expected number of instances.",
				1, childDataIndices.getX().size());
		Assert.assertEquals(
				"The number of instances assigned to the right child does not match the expected number of instances.",
				1, childDataIndices.getY().size());
		Assert.assertEquals("A wrong instance was assigned to the right children.", 0,
				childDataIndices.getY().get(0).intValue());
		Assert.assertEquals("A wrong instance was assigned to the left children.", 1,
				childDataIndices.getX().get(0).intValue());

		transformedFeatures = new double[][][] { { { 0, 1.2d }, { 1, 6d } }, { { 1.1d, 1.1d }, { 2, 0.5d } },
				{ { 1, 1.34d }, { 0, 3.2d } } };
		n = 2;
		k = 1;
		t1t2 = 0;
		threshold = 1.1d;

		childDataIndices = TimeSeriesTreeAlgorithm.getChildDataIndices(transformedFeatures, n, k, t1t2, threshold);

		Assert.assertEquals(
				"The number of instances assigned to both children does not match the expected number of instances.", n,
				childDataIndices.getX().size() + childDataIndices.getY().size());
		Assert.assertEquals(
				"The number of instances assigned to the left child does not match the expected number of instances.",
				2, childDataIndices.getX().size());
		Assert.assertEquals(
				"The number of instances assigned to the right child does not match the expected number of instances.",
				0, childDataIndices.getY().size());
		Assert.assertEquals("A wrong instance was assigned to the right children.", 0,
				childDataIndices.getX().get(0).intValue());
		Assert.assertEquals("A wrong instance was assigned to the right children.", 1,
				childDataIndices.getX().get(1).intValue());
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#tree(double[][], int[], double, TreeNode, int)}.
	 */
	@Test
	public void treeTest() throws TrainingException {
		TimeSeriesTree tst = new TimeSeriesTree(MAX_DEPTH, SEED, true);

		double[][] data = new double[][] { { 0, 1, 2, 3, 4, 5 }, { 0, 2, 4, 6, 8, 10 } };
		int[] targets = new int[] { 0, 1 };
		List<double[][]> dataList = new ArrayList<>();
		dataList.add(data);
		TimeSeriesDataset dataset = new TimeSeriesDataset(dataList, targets);
		
		tst.train(dataset);
		
		TreeNode<TimeSeriesTreeNodeDecisionFunction> rootNode = tst.getRootNode();
		Assert.assertEquals("The number of children of the root node does not match the expected number of children.",
				2, rootNode.getChildren().size());
	}
}
