package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.ml.tsc.filter.transform.ATransformFilter;
import jaicore.ml.tsc.filter.transform.HilbertTransform;

/**
 * Test suite for the {@link jaicore.ml.tsc.distances.TransformDistance}
 * implementation.
 * 
 * @author fischor
 */
public class TransformDistanceTest {

	/**
	 * Correctness test. Tests the distance calculation based on an defined input
	 * and expected output.
	 */
	@Test
	public void testCorrectnessForDistanceCalculation() {
		// Input.
		double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
		double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };
		// Expectation.
		double expectation = 0;

		TransformDistance td = new TransformDistance(0.5, new DynamicTimeWarping());
		double distance = td.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 0);
	}

	/**
	 * Correctness test. Tests the distance calculation based on an defined input
	 * and expected output.
	 */
	@Test
	public void testCorrectnessForDistanceCalculation2() {
		// Input.
		double[] timeSeries1 = { 1, 2, 3, 4, 5 }; // transform { -6.0833, -5.6666667, -4, -0.6666667, 6.41666667 }
		double[] timeSeries2 = { 2, 2, 2, 2, 2 }; // transform { -4.166667, -1.666667, 0, 1.666667, 4.166667 }
		double alpha = 0.5;
		ATransformFilter transform = new HilbertTransform();
		ITimeSeriesDistance euclideanDistance = new EuclideanDistance();

		// Expectation.
		double expectation = Math.cos(alpha) * Math.sqrt(15) + Math.sin(alpha) * 6.79562;

		TransformDistance td = new TransformDistance(alpha, transform, euclideanDistance);
		double distance = td.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 1.0E-5);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the distance
	 * measure, the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDistanceMeasure() {
		new TransformDistance(0.5, null);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the transform
	 * the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullTransform() {
		new TransformDistance(0.5, null, new EuclideanDistance(), new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>alpha > pi/2</code> the
	 * constuctor is supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForAlphaGreaterPiHalf() {
		double alpha = (Math.PI / 2) + 1e4;
		new TransformDistance(alpha, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>alpha < 0</code> the constuctor
	 * is supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForAlphaLessThanZero() {
		double alpha = 0 - Double.MIN_VALUE;
		new TransformDistance(alpha, new EuclideanDistance());
	}

	/**
	 * Boundary test: When initializing with <code>alpha = 0</code> the constructor
	 * is must not thrown an IllegalArgumentException.
	 */
	@Test
	public void testBoundaryForAlphaEqualToZero() {
		double alpha = 0;
		new TransformDistance(alpha, new EuclideanDistance());
	}

	/**
	 * Boundary test: When initializing with <code>alpha = pi/2</code> the
	 * constructor is must not thrown an IllegalArgumentException.
	 */
	@Test
	public void testBoundaryForAlphaEqualToPiHalf() {
		double alpha = Math.PI / 2;
		new TransformDistance(alpha, new EuclideanDistance());
	}

}