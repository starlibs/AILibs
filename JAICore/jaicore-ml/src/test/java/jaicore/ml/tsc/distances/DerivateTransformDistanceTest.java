package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.ml.tsc.filter.derivate.ADerivateFilter;
import jaicore.ml.tsc.filter.derivate.BackwardDifferenceDerivate;
import jaicore.ml.tsc.filter.transform.ATransformFilter;
import jaicore.ml.tsc.filter.transform.HilbertTransform;

/**
 * Test suite for the {@link jaicore.ml.tsc.distances.DerivateTransformDistance}
 * implementation.
 * 
 * @author fischor
 */
public class DerivateTransformDistanceTest {

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

		TransformDistance dtd = new TransformDistance(0.5, new DynamicTimeWarping());
		double distance = dtd.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 0);
	}

	/**
	 * Correctness test. Tests the distance calculation based on an defined input
	 * and expected output.
	 */
	@Test
	public void testCorrectnessForDistanceCalculation2() {
		// Input.
		double[] timeSeries1 = { 1, 2, 3, 4, 5 }; // transform { -6.0833, -5.6666667, -4, -0.6666667, 6.41666667 },
													// derivate { 1, 1, 1, 1}
		double[] timeSeries2 = { 2, 2, 2, 2, 2 }; // transform { -4.166667, -1.666667, 0, 1.666667, 4.166667 }, derivate
													// { 0, 0, 0, 0}
		double a = 0.5;
		double b = 0.25;
		double c = 0.25;
		ADerivateFilter derivate = new BackwardDifferenceDerivate();
		ATransformFilter transform = new HilbertTransform();
		ITimeSeriesDistance euclideanDistance = new EuclideanDistance();

		// Expectation.
		double expectation = a * Math.sqrt(15) + b * Math.sqrt(4) + c * 6.79562;

		DerivateTransformDistance dtd = new DerivateTransformDistance(a, b, c, derivate, transform, euclideanDistance);
		double distance = dtd.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 1.0E-5);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the distance
	 * measure, the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDistanceMeasure() {
		new DerivateTransformDistance(0.5, 0.25, 0.25, null);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the first
	 * distance measure, the constructor is supposed to throw an
	 * IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDistanceMeasure2() {
		new DerivateTransformDistance(0.5, 0.25, 0.25, null, new EuclideanDistance(), new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the second
	 * distance measure, the constructor is supposed to throw an
	 * IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDistanceMeasure3() {
		new DerivateTransformDistance(0.5, 0.25, 0.25, new EuclideanDistance(), null, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the third
	 * distance measure, the constructor is supposed to throw an
	 * IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDistanceMeasure4() {
		new DerivateTransformDistance(0.5, 0.25, 0.25, new EuclideanDistance(), new EuclideanDistance(), null);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the transform
	 * the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDerivate() {
		new DerivateTransformDistance(0.5, 0.25, 0.25, null, new HilbertTransform(), new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the derivation
	 * the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullTransform() {
		new DerivateTransformDistance(0.5, 0.25, 0.25, new BackwardDifferenceDerivate(), null, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>a > 1</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForAGreaterOne() {
		double a = 1.1;
		double b = 0.5;
		double c = 0.5;
		new DerivateTransformDistance(a, b, c, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>b > 1</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForBGreaterOne() {
		double a = 0.5;
		double b = 1.1;
		double c = 0.5;
		new DerivateTransformDistance(a, b, c, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>c > 1</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForCGreaterOne() {
		double a = 0.5;
		double b = 0.5;
		double c = 1.1;
		new DerivateTransformDistance(a, b, c, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>a < 0</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForALessThanZero() {
		double a = 0 - Double.MIN_VALUE;
		double b = 0.5;
		double c = 0.5;
		new DerivateTransformDistance(a, b, c, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>b < 0</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForBLessThanZero() {
		double a = 0.5;
		double b = 0 - Double.MIN_VALUE;
		double c = 0.5;
		new DerivateTransformDistance(a, b, c, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>c < 0</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForCLessThanZero() {
		double a = 0.5;
		double b = 0.5;
		double c = 0 - Double.MIN_VALUE;
		new DerivateTransformDistance(a, b, c, new EuclideanDistance());
	}

}