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
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.quality_measures.FStat;
import junit.framework.Assert;
import timeseriesweka.classifiers.ShapeletTransformClassifier;
import timeseriesweka.filters.shapelet_transforms.Shapelet;
import timeseriesweka.filters.shapelet_transforms.distance_functions.OnlineSubSeqDistance;
import timeseriesweka.filters.shapelet_transforms.distance_functions.SubSeqDistance;
import weka.core.DenseInstance;
import weka.core.Instance;

public class ShapeletTransformRefTest {

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

		// Initialize classifiers with values selected by reference classifier by
		// default
		final int k = 205;
		final int seed = 42;
		final int minShapeletLength = 3;
		final int maxShapeletLength = 24;// 24;
		ShapeletTransformTSClassifier ownClf = new ShapeletTransformTSClassifier(k, new FStat(), seed, false,
				minShapeletLength, maxShapeletLength);

		ShapeletTransformClassifier refClf = new ShapeletTransformClassifier();
		refClf.setNumberOfShapelets(k);
		refClf.setSeed(42);
		refClf.doSTransform(true);

		Map<String, Object> result = TSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
				new File(SYNTHETIC_CONTROL_TRAIN), new File(SYNTHETIC_CONTROL_TEST));

		System.out.println("Ref clf parameters: " + refClf.getParameters());
		System.out.println(result.toString());
	}

	@Test
	public void testSubseqDistance() {
		OnlineSubSeqDistance dist = new OnlineSubSeqDistance();
		Instance inst = new DenseInstance(1, new double[] { 1, 2, 3 });

		dist.setCandidate(inst, 0, 3, 1);

		dist.setShapelet(new Shapelet(dist.getCandidate()));
		long timeStart = System.currentTimeMillis();
		Assert.assertEquals(0.0d, dist.calculate(new double[] { 4, 2, 4, 6, 5 }, 0), EPS_DELTA);
		// System.out.println("Online took " + (System.currentTimeMillis() - timeStart)
		// + " ms.");

		SubSeqDistance dist2 = new SubSeqDistance();

		dist2.setCandidate(inst, 0, 3, 1);
		dist2.setShapelet(new Shapelet(dist2.getCandidate()));
		timeStart = System.currentTimeMillis();
		Assert.assertEquals(0.0d, dist2.calculate(new double[] { 4, 2, 4, 6, 5 }, 0), EPS_DELTA);
		// System.out.println("Normal took " + (System.currentTimeMillis() - timeStart)
		// + " ms.");

		// System.out.println(Arrays.toString(dist.zNormalise(new double[] { 2, 4, 6 },
		// false)));
	}

	public static void main(String[] args) throws FileNotFoundException, EvaluationException, TrainingException,
			PredictionException, ClassNotFoundException, IOException, TimeSeriesLoadingException {
		ShapeletTransformRefTest test = new ShapeletTransformRefTest();
		test.testClassifier();
	}
}
