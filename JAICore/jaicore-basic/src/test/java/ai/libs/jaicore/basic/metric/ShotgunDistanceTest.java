package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for the {@link aicore.ml.tsc.distances.ShotgunDistance}
 * implementation.
 *
 * @author fischor
 */
public class ShotgunDistanceTest {

	/**
	 * Correctness test. Tests the calculation based on an defined input and
	 * expected output.
	 */
	@Test
	public void testCorrectnessForDistanceCalculation() {
		// Input.
		double[] queryTimeSeries = { 1, 1, 2, 2, 3, 5, 3 };
		double[] sampleTimeSeries = { 1, 2, 3, 5, 5, 7 };
		// Expected output.
		double expectation = 0;

		int windowLength = 2;
		boolean meanNormalization = false;
		ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, meanNormalization);
		double distance = shotgunDistance.distance(queryTimeSeries, sampleTimeSeries);

		assertEquals(expectation, distance, 1e-5);
	}

	/**
	 * Correctness test. Tests the calculation based on an defined input and
	 * expected output.
	 */
	@Test
	public void testCorrectnessForDistanceCalculation2() {
		// Input.
		double[] queryTimeSeries = { 1, 1, 2, 2, 3, 5, 3 };
		double[] sampleTimeSeries = { 1, 2, 3, 5, 5, 7 };
		// Expected output.
		double expectation = .635109;

		int windowLength = 3;
		boolean meanNormalization = false;
		ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, meanNormalization);
		double distance = shotgunDistance.distance(queryTimeSeries, sampleTimeSeries);

		assertEquals(expectation, distance, 1e-5);
	}

	/**
	 * Robustness test: Do not break, when the window length is longer than any of
	 * the query or sample time series. Here the window length is longer than the
	 * query time series. The result of the distance calculation however, is
	 * irrelevant (undefined).
	 */
	@Test
	public void testRobustnessForTooLongWindowLength() {
		// Input.
		double[] queryTimeSeries = { 1, 2, 3 };
		double[] sampleTimeSeries = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		int windowLength = 5;
		ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, false);
		shotgunDistance.distance(queryTimeSeries, sampleTimeSeries);
		assertTrue(true); // this part must be reached
	}

	/**
	 * Robustness test: Do not break, when the window length is longer than any of
	 * the query or sample time series. Here the window length is longer than the
	 * sample time series. The result of the distance calculation however, is
	 * irrelevant (undefined).
	 */
	@Test
	public void testRobustnessForTooLongWindowLength2() {
		// Input.
		double[] queryTimeSeries = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		double[] sampleTimeSeries = { 1, 2, 3 };

		int windowLength = 5;
		ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, false);
		shotgunDistance.distance(queryTimeSeries, sampleTimeSeries);
		assertTrue(true); // this part must be reached
	}

	/**
	 * Robustness test: Do not break, when the window length is longer than any of
	 * the query or sample time series. Here the window length is longer than both
	 * time series. The result of the distance calculation however, is irrelevant
	 * (undefined).
	 */
	@Test
	public void testRobustnessForTooLongWindowLength3() {
		// Input.
		double[] queryTimeSeries = { 1, 2, 3 };
		double[] sampleTimeSeries = { 1, 1, 1 };

		int windowLength = 5;
		ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, false);
		shotgunDistance.distance(queryTimeSeries, sampleTimeSeries);
		assertTrue(true); // this part must be reached
	}

	/**
	 * Robustness test: When initializing with a negative window length, the
	 * constructor is supposed to throw an IllegalArgumentExpection.
	 */
	@Test
	public void testRobustnessForNegativeWindowLength() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			int windowLength = -1;
			new ShotgunDistance(windowLength, false);
		});
	}
}