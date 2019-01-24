package jaicore.ml.tsc.classifier.trees;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Level;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
import junit.framework.Assert;

public class TimeSeriesTreeTest {
	private static final double EPS_DELTA = 0.000001;

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesTreeTest.class);

	private static final String UNIVARIATE_PREFIX = "D:\\Data\\TSC\\UnivariateTSCProblems\\";
	private static final String ITALY_POWER_DEMAND_TRAIN = UNIVARIATE_PREFIX
			+ "ItalyPowerDemand\\ItalyPowerDemand_TRAIN.arff";
	private static final String ITALY_POWER_DEMAND_TEST = UNIVARIATE_PREFIX
			+ "ItalyPowerDemand\\ItalyPowerDemand_TEST.arff";

	// @Test
	public void classifierTest() throws TimeSeriesLoadingException, TrainingException, PredictionException {

		org.apache.log4j.Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		TimeSeriesTree tst = new TimeSeriesTree(10);

		Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader
				.loadArff(new File(ITALY_POWER_DEMAND_TRAIN));
		TimeSeriesDataset train = trainPair.getX();
		tst.setClassMapper(trainPair.getY());
		Pair<TimeSeriesDataset, ClassMapper> testPair = SimplifiedTimeSeriesLoader
				.loadArff(new File(ITALY_POWER_DEMAND_TEST));
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
				LOGGER.debug("Prediction {}: {} | Expected: {}", i, prediction, test.getTargets()[i]);
				if (prediction == test.getTargets()[i])
					correct++;
			}
		}

		double accuracy = (double) correct / totalPreds;

		final long evaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of classifier. Took {} ms. Accuracy: {}", (evaluationEnd - timeStart),
				accuracy);

	}

	@Test
	public void getModeTest() {
		int[] testArray = new int[] { 1, 2, 1, 1, 4, 6, 6, 6, 7, 7, 7, 7, 7, 7, 2, 1, 1 };
		Assert.assertEquals(7, TimeSeriesTreeAlgorithm.getMode(testArray));

		testArray = new int[] {};
		Assert.assertEquals(-1, TimeSeriesTreeAlgorithm.getMode(testArray));

		testArray = new int[] { 1, 1, 2, 2 };
		Assert.assertEquals(1, TimeSeriesTreeAlgorithm.getMode(testArray));
	}

	@Test
	public void calculateFeatureTest() {
		double[] instance = new double[] { 1, 2, 3 };
		// Mean
		Assert.assertEquals(2d, TimeSeriesTreeAlgorithm.calculateFeature(0, instance, 0, 3), EPS_DELTA);
		Assert.assertEquals(1.5d, TimeSeriesTreeAlgorithm.calculateFeature(0, instance, 0, 2), EPS_DELTA);
		// Standard deviation
		Assert.assertEquals(1d, TimeSeriesTreeAlgorithm.calculateFeature(1, instance, 0, 3), EPS_DELTA);
		// Slope
		Assert.assertEquals(1d, TimeSeriesTreeAlgorithm.calculateFeature(2, instance, 0, 3), EPS_DELTA);

		// TODO: Unify
		double[] features = TimeSeriesTreeAlgorithm.getFeatures(instance, 0, 3);
		Assert.assertEquals(2d, features[0], EPS_DELTA);
		Assert.assertEquals(1d, features[1], EPS_DELTA);
		Assert.assertEquals(1d, features[2], EPS_DELTA);
	}

	@Test
	public void randomlySampleNoReplacementTest() {
		int m = 40;
		int seed = 42;
		List<Integer> sampleBase = IntStream.range(0, 100).boxed().collect(Collectors.toList());

		List<Integer> samples = TimeSeriesTreeAlgorithm.randomlySampleNoReplacement(sampleBase, m, seed);
		Assert.assertEquals(m, samples.size());
	}

	@Test
	public void sampleIntervalsTest() {
		int m = 40;
		int seed = 42;

		Pair<List<Integer>, List<Integer>> result = TimeSeriesTreeAlgorithm.sampleIntervals(m, seed);

		Assert.assertEquals(result.getX().size(), result.getY().size());

		// TODO: Check this
		// Assert.assertEquals(m, result.getX().size());
	}

	@Test
	public void generateThresholdCandidatesTest() {
		// TODO
	}

	@Test
	public void transformInstancesTest() {
		// TODO
	}

	@Test
	public void calculateMarginTest() {
		// TODO
	}

	@Test
	public void calculateEntranceTest() {
		// TODO
	}

	@Test
	public void calculateDeltaEntropyTest() {
		// TODO
	}

	@Test
	public void getBestSplitIndexTest() {
		// TODO
	}

	@Test
	public void getChildDataIndicesTest() {
		// TODO
	}

	@Test
	public void treeTest() {
		// TODO
	}
}
