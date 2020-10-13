package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;

import org.api4.java.common.metric.IDistanceMetric;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.transform.vector.HilbertTransform;
import ai.libs.jaicore.basic.transform.vector.IVectorTransform;
import ai.libs.jaicore.basic.transform.vector.derivate.ADerivateFilter;
import ai.libs.jaicore.basic.transform.vector.derivate.BackwardDifferenceDerivate;

/**
 * Test suite for the {@link ai.libs.jaicore.basic.metric.DerivateTransformDistance}
 * implementation.
 *
 * @author fischor
 */
public class DerivateTransformDistanceTest {

	public static final EuclideanDistance EUCLIDEAN_DISTANCE = new EuclideanDistance();
	public static final BackwardDifferenceDerivate BACKWARD_DERIVATE = new BackwardDifferenceDerivate();

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
		IVectorTransform transform = new HilbertTransform();
		IDistanceMetric euclideanDistance = EUCLIDEAN_DISTANCE;

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
	@Test
	public void testRobustnessForNullDistanceMeasure() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateTransformDistance(0.5, 0.25, 0.25, null);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the first
	 * distance measure, the constructor is supposed to throw an
	 * IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullDistanceMeasure2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateTransformDistance(0.5, 0.25, 0.25, null, EUCLIDEAN_DISTANCE, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the second
	 * distance measure, the constructor is supposed to throw an
	 * IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullDistanceMeasure3() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateTransformDistance(0.5, 0.25, 0.25, EUCLIDEAN_DISTANCE, null, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the third
	 * distance measure, the constructor is supposed to throw an
	 * IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullDistanceMeasure4() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateTransformDistance(0.5, 0.25, 0.25, EUCLIDEAN_DISTANCE, EUCLIDEAN_DISTANCE, null);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the transform
	 * the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullDerivate() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateTransformDistance(0.5, 0.25, 0.25, null, new HilbertTransform(), EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the derivation
	 * the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullTransform() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DerivateTransformDistance(0.5, 0.25, 0.25, BACKWARD_DERIVATE, null, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>a > 1</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForAGreaterOne() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double a = 1.1;
			double b = 0.5;
			double c = 0.5;
			new DerivateTransformDistance(a, b, c, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>b > 1</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForBGreaterOne() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double a = 0.5;
			double b = 1.1;
			double c = 0.5;
			new DerivateTransformDistance(a, b, c, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>c > 1</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForCGreaterOne() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double a = 0.5;
			double b = 0.5;
			double c = 1.1;
			new DerivateTransformDistance(a, b, c, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>a < 0</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForALessThanZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double a = 0 - Double.MIN_VALUE;
			double b = 0.5;
			double c = 0.5;
			new DerivateTransformDistance(a, b, c, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>b < 0</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForBLessThanZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double a = 0.5;
			double b = 0 - Double.MIN_VALUE;
			double c = 0.5;
			new DerivateTransformDistance(a, b, c, EUCLIDEAN_DISTANCE);
		});
	}

	/**
	 * Robustness test: When initializing with <code>c < 0</code> the constuctor is
	 * supposed to thrown an IllegalArgumentException.
	 */
	@Test
	public void testRobustnessForCLessThanZero() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			double a = 0.5;
			double b = 0.5;
			double c = 0 - Double.MIN_VALUE;
			new DerivateTransformDistance(a, b, c, EUCLIDEAN_DISTANCE);
		});
	}

}