package jaicore.ml.tsc.classifier.shapelets;

import org.junit.Test;

import jaicore.ml.tsc.classifier.shapelets.LearnShapeletsAlgorithm;
import jaicore.ml.tsc.util.MathUtil;
import junit.framework.Assert;

/**
 * Unit tests for {@link LearnShapeletsAlgorithm}.
 * 
 * @author Julian Lienen
 *
 */
public class LearnShapeletsAlgorithmTest {
	/**
	 * Maximal delta for asserts with precision.
	 */
	private static final double EPS_DELTA = 0.00001;

	/**
	 * See {@link MathUtil#sigmoid(double)}.
	 */
	@Test
	public void sigmoidTest() {
		final double z = 0.5;
		final double expected = 0.6224593312018545646389;
		Assert.assertEquals(expected, MathUtil.sigmoid(z), EPS_DELTA);
	}

	/**
	 * See {@link LearnShapeletsAlgorithm#getNumberOfSegments(int, int, int)}.
	 */
	@Test
	public void getNumberOfSegmentsTest() {
		final int Q = 30;
		final int r = 5;
		final int minShapeLength = 2;

		// Actual implementation differs from paper's version due to index shift
		// correction
		Assert.assertEquals(30 - (5 + 1) * 2, LearnShapeletsAlgorithm.getNumberOfSegments(Q, minShapeLength, r));
	}

	/**
	 * See
	 * {@link LearnShapeletsAlgorithm#calculateD(double[][][], int, int, double[], int, int)}.
	 */
	@Test
	public void calculateDTest() {
		final double[][][] S = new double[][][] { { { 1, 2, 3 } } };
		final int minShapeLength = 3; // Length of 2 combined with r = 0
		final int r = 0; // Take first S array
		final double[] instance = new double[] { 1, 2, 3 };
		final int k = 0;
		final int j = 0; // Start with first index

		Assert.assertEquals(0d, LearnShapeletsAlgorithm.calculateD(S, minShapeLength, r, instance, k, j), EPS_DELTA);

		final double[] instance2 = new double[] { 2, 3, 4 }; // Shifted by one => Differs 1 per position

		final double expected = 3d / 3d;
		
		Assert.assertEquals(expected, LearnShapeletsAlgorithm.calculateD(S, minShapeLength, r, instance2, k, j),
				EPS_DELTA);
	}
	
	/**
	 * See
	 * {@link LearnShapeletsAlgorithm#calculateM_hat(double[][][], int, int, double[], int, int, double)}.
	 */
	@Test
	public void calculateM_hatTest() {
		final double[][][] S = new double[][][] { { { 1, 2, 3 } } };
		final int minShapeLength = 3; // Length of 2 combined with r = 0
		final int r = 0; // Take first S array
		final double[] instance = new double[] { 1, 2, 3, 4 };
		final int k = 0;
		final double alpha = -1d;

		// getNumberOfSegments(Q=4, minShapeLength=3, r=0) = 1
		Assert.assertEquals(0d,
				LearnShapeletsAlgorithm.calculateM_hat(S, minShapeLength, r, instance, k, instance.length, alpha));

		final double[] instance2 = new double[] { 1, 2, 3 };
		// getNumberOfSegments(Q=3, minShapeLength=3, r=0) = 0
		Assert.assertEquals(0d,
				LearnShapeletsAlgorithm.calculateM_hat(S, minShapeLength, r, instance2, k, instance2.length, alpha));
	}
}
