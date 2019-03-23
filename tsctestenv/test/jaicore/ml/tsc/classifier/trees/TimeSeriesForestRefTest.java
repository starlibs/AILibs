package jaicore.ml.tsc.classifier.trees;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.junit.Test;

import jaicore.basic.TimeOut;
import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.SimplifiedTSClassifierTest;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import timeseriesweka.classifiers.TSF;

/**
 * Reference tests for the {@link TimeSeriesForestClassifier}.
 * 
 * @author Julian Lienen
 *
 */
@SuppressWarnings("unused")
public class TimeSeriesForestRefTest {
	private static final double EPS_DELTA = 0.000001;

	private static final String UNIVARIATE_PREFIX = "data/univariate/";

	@Test
	public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
			PredictionException, IOException, TimeSeriesLoadingException {

		org.apache.log4j.Logger.getLogger("jaicore").setLevel(Level.DEBUG);

		String dataset = "Beef";
		final String trainPath = UNIVARIATE_PREFIX + dataset + File.separator + dataset + "_TRAIN.arff";
		final String testPath = UNIVARIATE_PREFIX + dataset + File.separator + dataset + "_TEST.arff";

		int seed = 42;
		int numTrees = 500;
		// Ref classifier uses no depth limit
		int maxDepth = 100;
		int numCPUs = 7;

		TimeSeriesForestClassifier ownClf = new TimeSeriesForestClassifier(numTrees, maxDepth, seed, false, numCPUs,
				new TimeOut(Integer.MAX_VALUE, TimeUnit.SECONDS));

		TSF refClf = new TSF(seed);
		refClf.setNumTrees(numTrees);

		Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, seed, null, null,
				new File(trainPath), new File(testPath));

		System.out.println("Ref clf parameters: " + refClf.getParameters());
		System.out.println(result.toString());
	}
}
