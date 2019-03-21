package jaicore.ml.tsc.classifier.trees;

import jaicore.ml.tsc.classifier.trees.TimeSeriesTree;

/**
 * Reference tests for the {@link TimeSeriesTree}.
 * 
 * @author Julian Lienen
 *
 */
@SuppressWarnings("unused")
public class TimeSeriesTreeRefTest {
	private static final double EPS_DELTA = 0.000001;

	private static final String UNIVARIATE_PREFIX = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\";

	private static final String CAR_TRAIN = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\Car\\Car_TRAIN.arff";
	private static final String CAR_TEST = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\Car\\Car_TEST.arff";

	private static final String ARROW_HEAD_TRAIN = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\ArrowHead\\ArrowHead\\ArrowHead_TRAIN.arff";
	private static final String ARROW_HEAD_TEST = "C:\\Users\\Julian\\Downloads\\UnivariateTSCProblems\\ArrowHead\\ArrowHead\\ArrowHead_TEST.arff";

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

	// @Test
	// public void testClassifier() throws FileNotFoundException,
	// EvaluationException, TrainingException,
	// PredictionException, IOException, TimeSeriesLoadingException,
	// ClassNotFoundException {
	//
	// Logger.getLogger("jaicore").setLevel(Level.DEBUG);
	//
	// System.out.println("Using OMP_NUM_THREADS=" +
	// System.getenv().get("OMP_NUM_THREADS"));
	//
	// TimeSeriesTree ownClf = new TimeSeriesTree(10);
	//
	// TSF timeSeriesForest = new TSF()
	//
	// Map<String, Object> result =
	// SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, seed, null,
	// null,
	// new File(COMPUTERS_TRAIN), new File(COMPUTERS_TEST));
	//
	// System.out.println("Ref clf parameters: " + refClf.getParameters());
	// System.out.println(result.toString());
	// }
}
