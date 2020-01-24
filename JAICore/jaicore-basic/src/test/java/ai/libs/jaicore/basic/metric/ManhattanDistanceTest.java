package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test suite for the {@link ai.libs.jaicore.basic.metric.ManhattanDistance}
 * implementation.
 *
 * @author fischor
 */
public class ManhattanDistanceTest {

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

		EuclideanDistance md = new EuclideanDistance();
		double distance = md.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 1.0E-5);
	}

	/**
	 * Correctness test. Tests the distance calculation based on an defined input
	 * and expected output.
	 */
	@Test
	public void testCorrectnessForDistanceCalculation2() {
		// Input.
		double[] timeSeries1 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
		double[] timeSeries2 = { 0.10, 0.10, 0.10, 0.10, 0.10 };
		// Expectation.
		double expectation = 3.29;

		ManhattanDistance md = new ManhattanDistance();
		double distance = md.distance(timeSeries1, timeSeries2);

		assertEquals(expectation, distance, 1.0E-5);
	}

}