package jaicore.ml.tsc.classifier.trees;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.SimplifiedTSClassifierTest;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
import timeseriesweka.classifiers.TSBF;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * Reference tests for {@link TimeSeriesBagOfFeaturesClassifier} objects.
 * 
 * Info: This was only used for experimental purposes. The tests performed in
 * the package <code>jaicore.ml.tsc.classifier</code> in the test environment
 * were used for empirical performance study.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesBagOfFeaturesRefTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesBagOfFeaturesRefTest.class);

	private static final String UNIVARIATE_PREFIX = "data/univariate/";

	// @Test
	public void compareClassifierPredictions()
			throws TimeSeriesLoadingException, Exception {

		String dataset = "Beef";
		final String trainPath = UNIVARIATE_PREFIX + dataset + File.separator + dataset + "_TRAIN.arff";
		final String testPath = UNIVARIATE_PREFIX + dataset + File.separator + dataset + "_TEST.arff";

		int seed = 2; // seedRandom.nextInt(100);

		int numBins = 20; // 1 + random.nextInt(20); // As in the reference implementation
		int numFolds = 15; // 3 + random.nextInt(15); // As in the reference implementation
		double zProp = 0.75; // z[i % z.length];// 0.01 + random.nextDouble(); // As in the reference
							// implementation


		int minIntervalLength = 5; // 2 + random.nextInt(10); // As in the reference implementation

		TimeSeriesBagOfFeaturesClassifier ownClf = new TimeSeriesBagOfFeaturesClassifier(seed, numBins, numFolds, zProp,
				minIntervalLength);

		TSBF refClf = new TSBF();
		refClf.seedRandom(seed);
		FieldUtils.writeField(refClf, "stepWise", false, true);
		FieldUtils.writeField(refClf, "numReps", 1, true);
		refClf.setParamSearch(false);
		refClf.searchParameters(false);

		Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader
				.loadArff(new File(trainPath));
		TimeSeriesDataset train = trainPair.getX();
		ownClf.setClassMapper(trainPair.getY());

		ArffReader arffReader = new ArffReader(new FileReader(new File(trainPath)));
		final Instances trainingInstances = arffReader.getData();
		trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);

		arffReader = new ArffReader(new FileReader(new File(testPath)));
		final Instances testInstances = arffReader.getData();
		testInstances.setClassIndex(testInstances.numAttributes() - 1);

		ownClf.train(train);
		refClf.buildClassifier(trainingInstances);
	}

	@Test
	public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
			PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException,
			IllegalAccessException {

		org.apache.log4j.Logger.getLogger("jaicore").setLevel(org.apache.log4j.Level.DEBUG);

		String dataset = "ItalyPowerDemand";
		final String trainPath = UNIVARIATE_PREFIX + dataset + "\\" + dataset + "_TRAIN.arff";
		final String testPath = UNIVARIATE_PREFIX + dataset + "\\" + dataset + "_TEST.arff";

		// int seed = 42;
		// int numBins = 20; // As in the reference implementation
		// int numFolds = 20; // As in the reference implementation
		// double zProp = 1; // As in the reference implementation
		// int minIntervalLength = 5; // As in the reference implementation

		double currBest = 0;
		// double[] z = new double[] { 0.1, 0.25, 0.5, 0.75 };

		int numTotalIterations = 1;
		for (int i = 0; i < numTotalIterations; i++) {
			int seed = 30; // seedRandom.nextInt(100);

			int numBins = 10; // 1 + random.nextInt(20); // As in the reference implementation
			int numFolds = 10; // 3 + random.nextInt(15); // As in the reference implementation
			double zProp = 0.7; // z[i % z.length];// 0.01 + random.nextDouble(); // As in the reference
								// implementation
			if (zProp > 1)
				zProp = 1d;
			int minIntervalLength = 5; // 2 + random.nextInt(10); // As in the reference implementation

			TimeSeriesBagOfFeaturesClassifier ownClf = new TimeSeriesBagOfFeaturesClassifier(seed, numBins, numFolds,
					zProp, minIntervalLength);

			TSBF refClf = new TSBF();
			refClf.seedRandom(seed);
			refClf.setZLevel(zProp);
			// FieldUtils.writeField(refClf, "stepWise", false, true);
			FieldUtils.writeField(refClf, "numReps", 1, true);
			refClf.setParamSearch(false);
			refClf.searchParameters(false);

			Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
					new File(trainPath), new File(testPath));
			if (((double) result.get("accuracy")) > currBest) {
				currBest = ((double) result.get("accuracy"));
				LOGGER.info(
						"New best score {} with numBins {}, numFolds {}, zProp {} and minIntervalLength {} (seed {}).",
						currBest, numBins, numFolds, zProp, minIntervalLength, seed);
			}
			
			LOGGER.debug(String.format("subSeries = %s.", Arrays.deepToString(ownClf.getSubsequences())));
			LOGGER.debug(String.format("intervals = %s.", Arrays.deepToString(ownClf.getIntervals())));
			LOGGER.debug(
					"Ref subseries: "
							+ Arrays.deepToString((int[][]) FieldUtils.readDeclaredField(refClf, "subSeries", true)));
			LOGGER.debug(
					"Ref intervals: "
							+ Arrays.deepToString((int[][][]) FieldUtils.readDeclaredField(refClf, "intervals", true)));

			if (i % 100 == 0)
				LOGGER.info("{}/{}", i, numTotalIterations);
		}

		LOGGER.info("Final best score: {}", currBest);
	}

	public static void main(String[] args) throws IllegalAccessException, FileNotFoundException, EvaluationException,
			TrainingException, PredictionException, IOException, TimeSeriesLoadingException {
		org.apache.log4j.Logger.getLogger("jaicore").setLevel(org.apache.log4j.Level.INFO);

		// int seed = 42;
		// int numBins = 20; // As in the reference implementation
		// int numFolds = 20; // As in the reference implementation
		// double zProp = 1; // As in the reference implementation
		// int minIntervalLength = 5; // As in the reference implementation

		String dataset = "Beef";
		final String trainPath = UNIVARIATE_PREFIX + dataset + "\\" + dataset + "_TRAIN.arff";
		final String testPath = UNIVARIATE_PREFIX + dataset + "\\" + dataset + "_TEST.arff";

		double currBest = 0;
		double[] z = new double[] { 0.1, 0.25, 0.5, 0.75 };

		int numTotalIterations = 1000;
		Random seedRandom = new Random(42);
		for (int i = 0; i < numTotalIterations; i++) {
			int seed = seedRandom.nextInt(100);

			int numBins = 10; // 1 + random.nextInt(20); // As in the reference implementation
			int numFolds = 10; // 3 + random.nextInt(15); // As in the reference implementation
			double zProp = z[i % z.length];// 0.01 + random.nextDouble(); // As in the reference implementation
			if (zProp > 1)
				zProp = 1d;
			int minIntervalLength = 5; // 2 + random.nextInt(10); // As in the reference implementation

			TimeSeriesBagOfFeaturesClassifier ownClf = new TimeSeriesBagOfFeaturesClassifier(seed, numBins, numFolds,
					zProp, minIntervalLength);

			TSBF refClf = new TSBF();
			refClf.seedRandom(seed);
			FieldUtils.writeField(refClf, "stepWise", false, true);
			FieldUtils.writeField(refClf, "numReps", 1, true);
			refClf.setParamSearch(false);
			refClf.searchParameters(false);

			Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
					new File(trainPath), new File(testPath));
			if (((double) result.get("accuracy")) > currBest) {
				currBest = ((double) result.get("accuracy"));
				LOGGER.info(
						"New best score {} with numBins {}, numFolds {}, zProp {} and minIntervalLength {} (seed {}).",
						currBest, numBins, numFolds, zProp, minIntervalLength, seed);
			}

			if (i % 100 == 0)
				LOGGER.info("{}/{}", i, numTotalIterations);
		}

		LOGGER.info("Final best score: {}", currBest);
	}
}
