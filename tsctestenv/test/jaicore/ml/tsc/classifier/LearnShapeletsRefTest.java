package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.nd4j.linalg.api.buffer.DataBuffer.Type;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import timeseriesweka.classifiers.LearnShapelets;

public class LearnShapeletsRefTest {

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

	@Test
	public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
			PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {



		Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		System.out.println("Using OMP_NUM_THREADS=" + System.getenv().get("OMP_NUM_THREADS"));

		Nd4j.setDataType(Type.DOUBLE);

		// Initialize classifiers with values selected by reference classifier by
		// default
		int Q = 25;
		int K = 8;
		double learningRate = 0.1;
		double regularization = 0.01;
		int scaleR = 3;
		int minShapeLength = 4; // (int) (0.2d * Q);
		int maxIter = 300;
		int seed = 42;

		LearnShapeletsClassifier ownClf = new LearnShapeletsClassifier(K, learningRate, regularization, scaleR,
				minShapeLength, maxIter, seed);

		LearnShapelets refClf = new LearnShapelets();
		refClf.setSeed(seed);
		refClf.fixParameters();

		Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
				new File(ITALY_POWER_DEMAND_TRAIN), new File(ITALY_POWER_DEMAND_TEST));

		System.out.println("Ref clf parameters: " + refClf.getParameters());
		System.out.println(result.toString());
	}
}
