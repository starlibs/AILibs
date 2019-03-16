package jaicore.ml.tsc.classifier.trees;

import java.io.File;
import java.util.List;

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

/**
 * Unit tests of the time series forest classifier.
 * 
 * @author Julian Lienen
 *
 */
@SuppressWarnings("unused")
public class TimeSeriesForestTest {

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

	private static final String COFFEE_TRAIN = UNIVARIATE_PREFIX + "Coffee\\Coffee_TRAIN.arff";
	private static final String COFFEE_TEST = UNIVARIATE_PREFIX + "Coffee\\Coffee_TEST.arff";

	private static final String BEEF_TRAIN = UNIVARIATE_PREFIX + "Beef\\Beef_TRAIN.arff";
	private static final String BEEF_TEST = UNIVARIATE_PREFIX + "Beef\\Beef_TEST.arff";

	/**
	 * Test for the (possibly parallel) training of the time series forest
	 * classifier.
	 * 
	 * @throws TimeSeriesLoadingException
	 * @throws TrainingException
	 * @throws PredictionException
	 */
	@Test
	public void classifierTest() throws TimeSeriesLoadingException, TrainingException, PredictionException {

		org.apache.log4j.Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		TimeSeriesForestClassifier tsf = new TimeSeriesForestClassifier(500, 1000, 42);

		Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader
				.loadArff(new File(ITALY_POWER_DEMAND_TRAIN));
		TimeSeriesDataset train = trainPair.getX();
		tsf.setClassMapper(trainPair.getY());
		Pair<TimeSeriesDataset, ClassMapper> testPair = SimplifiedTimeSeriesLoader
				.loadArff(new File(ITALY_POWER_DEMAND_TEST));
		TimeSeriesDataset test = testPair.getX();

		// Training
		LOGGER.debug("Starting training of classifier...");
		long timeStart = System.currentTimeMillis();
		tsf.train(train);
		final long trainingEnd = System.currentTimeMillis();
		LOGGER.debug("Finished training of classifier. Took {} ms.", (trainingEnd - timeStart));

		// Evaluation
		LOGGER.debug("Starting evaluation of classifier...");
		timeStart = System.currentTimeMillis();

		List<?> predictions = tsf.predict(test);
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
}
