package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
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
	EuclideanDistance euclideanDistance;

	/** The complexity measure used throughout the tests. */
	StretchingComplexity stretchingComplexity;

	@Before
	public void setUp() {
		this.euclideanDistance = new EuclideanDistance();
		this.stretchingComplexity = new StretchingComplexity();
	}

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
		double expectation = this.euclideanDistance.distance(timeSeries1, timeSeries2) * (15 / 5);

		ComplexityInvariantDistance cid = new ComplexityInvariantDistance(this.euclideanDistance, this.stretchingComplexity);
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
			new ComplexityInvariantDistance(null, this.stretchingComplexity);
		});
	}

	/**
	 * Robustness test: When initializing with <code>null</code> for the complexity
	 * measure, the constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNullComplexityMeasure() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new ComplexityInvariantDistance(this.euclideanDistance, null);
		});
	}

}