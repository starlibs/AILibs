package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.basic.complexity.StretchingComplexity;

/**
 * Test suite for the
 * {@link ai.libs.jaicore.basic.metric.ComplexityInvariantDistance} implementation.
 *
 * @author fischor
 */
public class ComplexityInvariantDistanceTest {

	/** The distance measure used throughout the tests. */
	private static final EuclideanDistance EUCLIDEAN_DISTANCE = new EuclideanDistance();

	/** The complexity measure used throughout the tests. */
	private static final StretchingComplexity STRECHING_COMPLEXITY = new StretchingComplexity();

	/**
	 * Correctness test. Tests the distance calculation based on an defined input
	 * and expected output.
	 */
	@Test
	public void testCorrectnessForDistanceCalculation() {
		// Input.
		double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 }; // complexity 5
		double[] timeSeries2 = { .0, Math.sqrt(8), .0, Math.sqrt(8), .0, Math.sqrt(8) }; // complexity 15
		// Expectation.
		double expectation = EUCLIDEAN_DISTANCE.distance(timeSeries1, timeSeries2) * (15 / 5);

		ComplexityInvariantDistance cid = new ComplexityInvariantDistance(EUCLIDEAN_DISTANCE, STRECHING_COMPLEXITY);
		double distance = cid.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 0.001);
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the distance
	 * measure, the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullDistanceMeasure() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new ComplexityInvariantDistance(null, STRECHING_COMPLEXITY);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the complexity
	 * measure, the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullComplexityMeasure() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new ComplexityInvariantDistance(EUCLIDEAN_DISTANCE, null);
		});
	}

}