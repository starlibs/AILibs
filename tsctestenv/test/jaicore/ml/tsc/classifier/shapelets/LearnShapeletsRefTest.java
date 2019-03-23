package jaicore.ml.tsc.classifier.shapelets;

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
import jaicore.ml.tsc.classifier.SimplifiedTSClassifierTest;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import timeseriesweka.classifiers.LearnShapelets;

/**
 * Reference tests for the {@link LearnShapeletsClassifier}.
 * 
 * @author Julian Lienen
 *
 */
public class LearnShapeletsRefTest {

	private static final String UNIVARIATE_PREFIX = "data/univariate/";

	@Test
	public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
			PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {

		// TODO: Change this?
		Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		String dataset = "ItalyPowerDemand";
		final String trainPath = UNIVARIATE_PREFIX + dataset + File.separator + dataset + "_TRAIN.arff";
		final String testPath = UNIVARIATE_PREFIX + dataset + File.separator + dataset + "_TEST.arff";

		// Initialize classifiers with values selected by reference classifier by
		// default
		// int Q = 25;
		int K = 8;
		double learningRate = 0.1;
		double regularization = 0.01;
		int scaleR = 3;
		double minShapeLength = 0.2; // (int) (0.2d * Q);
		int maxIter = 600;
		int seed = 42;

		LearnShapeletsClassifier ownClf = new LearnShapeletsClassifier(K, learningRate, regularization, scaleR,
				minShapeLength, maxIter, seed);
		// Use same K as in reference implementation
		ownClf.setEstimateK(true);

		LearnShapelets refClf = new LearnShapelets();
		refClf.setSeed(seed);
		refClf.fixParameters();

		Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
				new File(trainPath), new File(testPath));

		System.out.println("Ref clf parameters: " + refClf.getParameters());
		System.out.println(result.toString());
	}
}
