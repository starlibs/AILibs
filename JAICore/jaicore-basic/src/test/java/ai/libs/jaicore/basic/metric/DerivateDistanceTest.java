package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.api4.java.common.metric.IDistanceMetric;
import org.junit.Test;

import ai.libs.jaicore.basic.metric.DerivateDistance;
import ai.libs.jaicore.basic.metric.DynamicTimeWarping;
import ai.libs.jaicore.basic.metric.EuclideanDistance;

/**
 * Test suite for the {@link ai.libs.jaicore.basic.metric.DerivateDistance}
 * implementation.
 *
 * @author fischor
 */
public class DerivateDistanceTest {

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

		DerivateDistance dtd = new DerivateDistance(0.5, new DynamicTimeWarping());
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
		double[] timeSeries1 = { 1, 1, 2, 2, 3, 5 };
		double[] timeSeries2 = { 1, 2, 3, 5, 5, 6 };
		double alpha = 0.5;
		IDistanceMetric timeSeriesDistance = new DynamicTimeWarping();
		IDistanceMetric derivateDistance = new EuclideanDistance();

		// Expectation.
		double expectation = Math.cos(alpha) * 1 + Math.sin(alpha) * Math.sqrt(7);

		DerivateDistance dtd = new DerivateDistance(alpha, timeSeriesDistance, derivateDistance);
		double distance = dtd.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 1.0E-5);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the distance
	 * measure, the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDistanceMeasure() {
		new DerivateDistance(0.5, null);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the derivation
	 * the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForNullDerivationMeasure() {
		new DerivateDistance(0.5, null, new EuclideanDistance(), new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>alpha > pi/2</code> the
	 * constuctor is supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForAlphaGreaterPiHalf() {
		double alpha = (Math.PI / 2) + 1e4;
		new DerivateDistance(alpha, new EuclideanDistance());
	}

	/**
	 * Robustness test: When initializing with <code>alpha < 0</code> the constuctor
	 * is supposed to thrown an IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForAlphaLessThanZero() {
		double alpha = 0 - Double.MIN_VALUE;
		new DerivateDistance(alpha, new EuclideanDistance());
	}

	/**
	 * Boundary test: When initializing with <code>alpha = 0</code> the constructor
	 * is must not thrown an IllegalArgumentException.
	 */
	@Test
	public void testBoundaryForAlphaEqualToZero() {
		new DerivateDistance(0, new EuclideanDistance());
		assertTrue(true); // this part must be reached
	}

	/**
	 * Boundary test: When initializing with <code>alpha = pi/2</code> the
	 * constructor is must not thrown an IllegalArgumentException.
	 */
	@Test
	public void testBoundaryForAlphaEqualToPiHalf() {
		double alpha = Math.PI / 2;
		new DerivateDistance(alpha, new EuclideanDistance());
		assertTrue(true); // this part must be reached
	}

}