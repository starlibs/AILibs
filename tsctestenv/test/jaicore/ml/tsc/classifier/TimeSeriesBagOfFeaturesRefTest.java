package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.trees.TimeSeriesBagOfFeaturesClassifier;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import timeseriesweka.classifiers.TSBF;

/**
 * Reference tests for {@link TimeSeriesBagOfFeaturesClassifier} objects.
 * 
 * @author Julian Lienen
 *
 */
@SuppressWarnings("unused")
public class TimeSeriesBagOfFeaturesRefTest {
	private static final String UNIVARIATE_PREFIX = "D:\\Data\\TSC\\UnivariateTSCProblems\\";

	private static final String CAR_TRAIN = UNIVARIATE_PREFIX + "UnivariateTSCProblems\\Car\\Car_TRAIN.arff";
	private static final String CAR_TEST = UNIVARIATE_PREFIX + "UnivariateTSCProblems\\Car\\Car_TEST.arff";

	private static final String ARROW_HEAD_TRAIN = UNIVARIATE_PREFIX
			+ "UnivariateTSCProblems\\ArrowHead\\ArrowHead\\ArrowHead_TRAIN.arff";
	private static final String ARROW_HEAD_TEST = UNIVARIATE_PREFIX
			+ "UnivariateTSCProblems\\ArrowHead\\ArrowHead\\ArrowHead_TEST.arff";

	private static final String ITALY_POWER_DEMAND_TRAIN = UNIVARIATE_PREFIX
			+ "ItalyPowerDemand\\ItalyPowerDemand_TRAIN.arff";
	private static final String ITALY_POWER_DEMAND_TEST = UNIVARIATE_PREFIX
			+ "ItalyPowerDemand\\ItalyPowerDemand_TEST.arff";

	private static final String RACKET_SPORTS_TRAIN = UNIVARIATE_PREFIX + "RacketSports\\RacketSports_TRAIN.arff";
	private static final String RACKET_SPORTS_TEST = UNIVARIATE_PREFIX + "RacketSports\\RacketSports_TEST.arff";

	private static final String SYNTHETIC_CONTROL_TRAIN = UNIVARIATE_PREFIX
			+ "\\SyntheticControl\\SyntheticControl_TRAIN.arff";
	private static final String SYNTHETIC_CONTROL_TEST = UNIVARIATE_PREFIX
			+ "\\SyntheticControl\\SyntheticControl_TEST.arff";

	private static final String COMPUTERS_TRAIN = UNIVARIATE_PREFIX + "\\Computers\\Computers_TRAIN.arff";
	private static final String COMPUTERS_TEST = UNIVARIATE_PREFIX + "\\Computers\\Computers_TEST.arff";

	@Test
	public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
			PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {

		// TODO: Change this?
		Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		int seed = 42;
		int numBins = 10; // As in the reference implementation
		int numFolds = 10; // As in the reference implementation
		double zProp = 1; // As in the reference implementation
		int minIntervalLength = 5; // As in the reference implementation

		TimeSeriesBagOfFeaturesClassifier ownClf = new TimeSeriesBagOfFeaturesClassifier(seed, numBins, numFolds,
				zProp, minIntervalLength);

		TSBF refClf = new TSBF();
		refClf.seedRandom(seed);

		Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
				new File(ITALY_POWER_DEMAND_TRAIN), new File(ITALY_POWER_DEMAND_TEST));

		System.out.println("Ref clf parameters: " + refClf.getParameters());
		System.out.println(result.toString());
	}
}
