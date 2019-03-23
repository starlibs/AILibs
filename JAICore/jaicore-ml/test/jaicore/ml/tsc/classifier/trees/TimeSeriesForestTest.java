package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

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

	/**
	 * Log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesTreeTest.class);

	/**
	 * Tests the training of TSF.
	 * 
	 * @throws TrainingException
	 *             thrown if the training could not be performed.
	 */
	@Test
	public void voidTrainingTest() throws TrainingException {
		int numTrees = 10;
		int maxDepth = 10;
		int seed = 42;
		
		TimeSeriesForestClassifier classifier = new TimeSeriesForestClassifier(numTrees, maxDepth, seed);
		
		final double[][] data = new double[][] {{1,2,3,4,5}, {2,3,4,5,6}};
		ArrayList<double[][]> valueMatrix = new ArrayList<>();
		valueMatrix.add(data);
		final int[] targets = new int[] {0,1};
		
		classifier.train(new TimeSeriesDataset(valueMatrix, targets));
		Assert.assertEquals("The number of trained trees does not match the expected number.", numTrees,
				classifier.getTrees().length);
	}
}
