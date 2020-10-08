package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.api4.java.common.metric.IDistanceMetric;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for the {@link ai.libs.jaicore.basic.metric.DerivateDistance}
 * implementation.
 *
 * @author fischor
 */
public class DerivateDistanceTest {

	public static final EuclideanDistance EUCLIDEAN_DISTANCE = new EuclideanDistance();

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
		IDistanceMetric derivateDistance = EUCLIDEAN_DISTANCE;

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
	@Test
	public void testRobustnessForNullDistanceMeasure() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateDistance(0.5, null);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the derivation
	 * the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullDerivationMeasure() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateDistance(0.5, null, EUCLIDEAN_DISTANCE, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>alpha > pi/2</code> the
	 * constuctor is supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForAlphaGreaterPiHalf() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double alpha = (Math.PI / 2) + 1e4;
			new DerivateDistance(alpha, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>alpha < 0</code> the constuctor
	 * is supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForAlphaLessThanZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double alpha = 0 - Double.MIN_VALUE;
			new DerivateDistance(alpha, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Boundary test: When initializing with <code>alpha = 0</code> the constructor
	 * is must not thrown an IllegalArgumentException.
	 */
	@Test
	public void testBoundaryForAlphaEqualToZero() {
		new DerivateDistance(0, EUCLIDEAN_DISTANCE);
		assertTrue(true); // this part must be reached
	}

	/**
	 * Boundary test: When initializing with <code>alpha = pi/2</code> the
	 * constructor is must not thrown an IllegalArgumentException.
	 */
	@Test
	public void testBoundaryForAlphaEqualToPiHalf() {
		double alpha = Math.PI / 2;
		new DerivateDistance(alpha, EUCLIDEAN_DISTANCE);
		assertTrue(true); // this part must be reached
	}

}