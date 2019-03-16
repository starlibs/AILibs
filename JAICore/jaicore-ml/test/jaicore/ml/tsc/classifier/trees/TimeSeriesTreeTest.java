package jaicore.ml.tsc.classifier.trees;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Level;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.TreeNode;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.trees.TimeSeriesTree.TimeSeriesTreeNodeDecisionFunction;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.features.TimeSeriesFeature;
import jaicore.ml.tsc.features.TimeSeriesFeature.FeatureType;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesTreeTest.class);

	private static final String UNIVARIATE_PREFIX = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\";

	private static final String ITALY_POWER_DEMAND_TRAIN = UNIVARIATE_PREFIX
			+ "ItalyPowerDemand\\ItalyPowerDemand_TRAIN.arff";
	private static final String ITALY_POWER_DEMAND_TEST = UNIVARIATE_PREFIX
			+ "ItalyPowerDemand\\ItalyPowerDemand_TEST.arff";

	private static final String BEEF_TRAIN = UNIVARIATE_PREFIX + "Beef\\Beef_TRAIN.arff";
	private static final String BEEF_TEST = UNIVARIATE_PREFIX + "Beef\\Beef_TEST.arff";

	/**
	 * Tests the training of time series trees.
	 * 
	 * @throws TimeSeriesLoadingException
	 * @throws TrainingException
	 * @throws PredictionException
	 */
	@Test
	public void classifierTest() throws TimeSeriesLoadingException, TrainingException, PredictionException {

		org.apache.log4j.Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		TimeSeriesTree tst = new TimeSeriesTree(1000, 42);

		Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader
				.loadArff(new File(BEEF_TRAIN));
		TimeSeriesDataset train = trainPair.getX();
		tst.setClassMapper(trainPair.getY());
		Pair<TimeSeriesDataset, ClassMapper> testPair = SimplifiedTimeSeriesLoader
				.loadArff(new File(BEEF_TEST));
		TimeSeriesDataset test = testPair.getX();

		// Training
		LOGGER.debug("Starting training of classifier...");
		long timeStart = System.currentTimeMillis();
		tst.train(train);
		final long trainingEnd = System.currentTimeMillis();
		LOGGER.debug("Finished training of classifier. Took {} ms.", (trainingEnd - timeStart));

		// Evaluation
		LOGGER.debug("Starting evaluation of classifier...");
		timeStart = System.currentTimeMillis();

		List<?> predictions = tst.predict(test);
		int totalPreds = predictions.size();
		int correct = 0;

		if (totalPreds <= 0)
			throw new PredictionException("Nothing has been predicted.");

		if (!(predictions.get(0) instanceof Integer || predictions.get(0) instanceof String))
			throw new PredictionException("Can not evaluate classifier due to an unsupported target type.");

		if (predictions.get(0) instanceof Integer) {
			for (int i = 0; i < totalPreds; i++) {
				int prediction = (int) predictions.get(i);
				// LOGGER.debug("Prediction {}: {} | Expected: {}", i, prediction,
				// test.getTargets()[i]);
				if (prediction == test.getTargets()[i])
					correct++;
			}
		}

		double accuracy = (double) correct / totalPreds;

		final long evaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of classifier. Took {} ms. Accuracy: {}", (evaluationEnd - timeStart),
				accuracy);

	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#calculateFeature(FeatureType, double[], int, int, boolean)}.
	 */
	@Test
	public void calculateFeatureTest() {
		double[] instance = new double[] { 1, 2, 3 };
		// Mean
		Assert.assertEquals(2d, TimeSeriesFeature.calculateFeature(FeatureType.MEAN, instance, 0, 2,
				TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION), EPS_DELTA);
		Assert.assertEquals(1.5d,
				TimeSeriesFeature.calculateFeature(FeatureType.MEAN, instance, 0, 1,
						TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION),
				EPS_DELTA);
		// Standard deviation
		Assert.assertEquals(1d,
				TimeSeriesFeature.calculateFeature(FeatureType.STDDEV, instance, 0, 2,
						TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION),
				EPS_DELTA);
		// Slope
		Assert.assertEquals(1d, TimeSeriesFeature.calculateFeature(FeatureType.SLOPE, instance, 0, 2,
				TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION), EPS_DELTA);

		double[] features = TimeSeriesFeature.getFeatures(instance, 0, 2,
				TimeSeriesTreeAlgorithm.USE_BIAS_CORRECTION);
		Assert.assertEquals(2d, features[0], EPS_DELTA);
		Assert.assertEquals(1d, features[1], EPS_DELTA);
		Assert.assertEquals(1d, features[2], EPS_DELTA);
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#randomlySampleNoReplacement(List, int, int)}.
	 */
	@Test
	public void randomlySampleNoReplacementTest() {
		int m = 40;
		int seed = 42;
		List<Integer> sampleBase = IntStream.range(0, 100).boxed().collect(Collectors.toList());

		List<Integer> samples = TimeSeriesTreeAlgorithm.randomlySampleNoReplacement(sampleBase, m, seed);
		Assert.assertEquals(m, samples.size());
	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#sampleIntervals(int, int)}.
	 */
	@Test
	public void sampleIntervalsTest() {
		int m = 40;
		int seed = 42;

		Pair<List<Integer>, List<Integer>> result = TimeSeriesTreeAlgorithm.sampleIntervals(m, seed);

		Assert.assertEquals(result.getX().size(), result.getY().size());
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

		Assert.assertEquals(3, thresholdCandidates.size());
		Assert.assertEquals(numOfCandidates, thresholdCandidates.get(0).size());
		Assert.assertEquals(numOfCandidates, thresholdCandidates.get(1).size());
		Assert.assertEquals(numOfCandidates, thresholdCandidates.get(2).size());

		Assert.assertEquals(6d / 5d, thresholdCandidates.get(0).get(1), EPS_DELTA);
		Assert.assertEquals(6d / 5d + 2d, thresholdCandidates.get(1).get(1), EPS_DELTA);
		Assert.assertEquals(4d / 5d, thresholdCandidates.get(2).get(1), EPS_DELTA);
	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#transformInstances(double[][], Pair)}.
	 */
	@Test
	public void transformInstancesTest() {
		TimeSeriesTreeAlgorithm algorithm = new TimeSeriesTreeAlgorithm(0, 0);

		double[][] data = new double[][] { { 0, 1, 2, 3, 4, 5, 6 }, { 2, 4, 6, 8, 10, 12, 14 } };
		List<Integer> T1 = Arrays.asList(0, 0);
		List<Integer> T2 = Arrays.asList(1, 2);
		Pair<List<Integer>, List<Integer>> T1T2 = new Pair<>(T1, T2);

		double[][][] transformedData = algorithm.transformInstances(data, T1T2);

		Assert.assertEquals(3, transformedData.length);
		Assert.assertEquals(T1.size(), transformedData[0].length);
		Assert.assertEquals(T2.size(), transformedData[0].length);
		Assert.assertEquals(data.length, transformedData[0][0].length);

		Assert.assertEquals(0.5d, transformedData[0][0][0], EPS_DELTA); // Mean of first two elements of first
																			// instance
		Assert.assertEquals(1d, transformedData[0][1][0], EPS_DELTA); // Mean of first three elements of first
																			// instance

		Assert.assertEquals(Math.sqrt(0.5d), transformedData[1][0][0], EPS_DELTA); // Stddev of first two elements of
																						// first
																			// instance
		Assert.assertEquals(Math.sqrt(1d), transformedData[1][1][0], EPS_DELTA); // Stddev of first three elements of
																					// first
																			// instance

		Assert.assertEquals(1d, transformedData[2][0][0], EPS_DELTA); // Stddev of first two elements of
																						// first instance
		Assert.assertEquals(2d, transformedData[2][1][1], EPS_DELTA); // Stddev of first three elements of
																			// second instance
	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#calculateMargin(double[], double)}.
	 */
	@Test
	public void calculateMarginTest() {
		double[] dataValues = new double[] { 0, 1, 2, 3, 4, 5 };
		double thresholdCandidate = 1.5d;
		Assert.assertEquals(0.5d, TimeSeriesTreeAlgorithm.calculateMargin(dataValues, thresholdCandidate), EPS_DELTA);

		dataValues = new double[] { 2, 4, 6, 7 };
		thresholdCandidate = 0d;
		Assert.assertEquals(2d, TimeSeriesTreeAlgorithm.calculateMargin(dataValues, thresholdCandidate), EPS_DELTA);
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
		
		Assert.assertEquals(1d - TimeSeriesTreeAlgorithm.ENTROPY_APLHA * 0,
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

		Assert.assertEquals(parentEntropy + 6d / 8d * Math.log(1d) + 2d / 8d * 0d, TimeSeriesTreeAlgorithm
				.calculateDeltaEntropy(dataValues, targets, thresholdCandidate, classes, parentEntropy), EPS_DELTA);

	}

	/**
	 * See {@link TimeSeriesTreeAlgorithm#getBestSplitIndex(double[])}.
	 */
	@Test
	public void getBestSplitIndexTest() {
		TimeSeriesTreeAlgorithm algorithm = new TimeSeriesTreeAlgorithm(0, 0);
		
		double[] deltaEntropyStarPerFeatureType = new double[] { 1, 6, 7 };
		Assert.assertEquals(2, algorithm.getBestSplitIndex(deltaEntropyStarPerFeatureType));

		deltaEntropyStarPerFeatureType = new double[] { 2, 0.01, -1 };
		Assert.assertEquals(0, algorithm.getBestSplitIndex(deltaEntropyStarPerFeatureType));
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

		Assert.assertEquals(n, childDataIndices.getX().size() + childDataIndices.getY().size());
		Assert.assertEquals(1, childDataIndices.getX().size());
		Assert.assertEquals(1, childDataIndices.getY().size());
		Assert.assertEquals(0, childDataIndices.getY().get(0).intValue());
		Assert.assertEquals(1, childDataIndices.getX().get(0).intValue());

		transformedFeatures = new double[][][] { { { 0, 1.2d }, { 1, 6d } }, { { 1.1d, 1.1d }, { 2, 0.5d } },
				{ { 1, 1.34d }, { 0, 3.2d } } };
		n = 2;
		k = 1;
		t1t2 = 0;
		threshold = 1.1d;

		childDataIndices = TimeSeriesTreeAlgorithm.getChildDataIndices(transformedFeatures, n, k, t1t2, threshold);

		Assert.assertEquals(n, childDataIndices.getX().size() + childDataIndices.getY().size());
		Assert.assertEquals(2, childDataIndices.getX().size());
		Assert.assertEquals(0, childDataIndices.getY().size());
		Assert.assertEquals(0, childDataIndices.getX().get(0).intValue());
		Assert.assertEquals(1, childDataIndices.getX().get(1).intValue());
	}

	/**
	 * See
	 * {@link TimeSeriesTreeAlgorithm#tree(double[][], int[], double, TreeNode, int)}.
	 */
	@Test
	public void treeTest() throws TrainingException {
		// TODO
		TimeSeriesTree tst = new TimeSeriesTree(10, 42, true);

		double[][] data = new double[][] { { 0, 1, 2, 3, 4, 5 }, { 0, 2, 4, 6, 8, 10 } };
		int[] targets = new int[] { 0, 1 };
		List<double[][]> dataList = new ArrayList<>();
		dataList.add(data);
		TimeSeriesDataset dataset = new TimeSeriesDataset(dataList, targets);
		
		tst.train(dataset);
		
		TreeNode<TimeSeriesTreeNodeDecisionFunction> rootNode = tst.getRootNode();
		Assert.assertEquals(2, rootNode.getChildren().size());

		System.out.println(rootNode.getValue());
	}
}
