package jaicore.ml.tsc.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimeSeriesUtilTest {

	double[] T = { 1, 2, 3, 4, 5, 6, 7, 8 };
	double[] U = { 1, 1, 1, 1, 1, 1 };

	@Test
	public void testMean() {
		double mean = TimeSeriesUtil.mean(T);
		double expectation = 4.5;
		assertEquals(expectation, mean, .0);
	}

	@Test
	public void testMean2() {
		double mean = TimeSeriesUtil.mean(U);
		double expectation = 1;
		assertEquals(expectation, mean, .0);
	}

	@Test
	public void testVariance() {
		double variance = TimeSeriesUtil.variance(T);
		double expectation = (2 * 3.5 * 3.5 + 2 * 2.5 * 2.5 + 2 * 1.5 * 1.5 + 0.5) / 8;
		assertEquals(expectation, variance, 1e-5);
	}

	@Test
	public void testVariance2() {
		double variance = TimeSeriesUtil.variance(U);
		double expectation = 0;
		assertEquals(expectation, variance, 1e-5);
	}

	@Test
	public void testStandardDeviation() {
		double standardDeviation = TimeSeriesUtil.standardDeviation(T);
		double expectation = Math.sqrt(5.25);
		assertEquals(expectation, standardDeviation, 1e-5);
	}

	@Test
	public void testStandardDeviation2() {
		double standardDeviation = TimeSeriesUtil.standardDeviation(U);
		double expectation = 0;
		assertEquals(expectation, standardDeviation, .0);
	}

	@Test
	public void testZTransformWithzeroStandardDeviation() {
		double[] zTransformed = TimeSeriesUtil.zTransform(U);
		double[] expectation = { 0, 0, 0, 0, 0, 0 };

		String message = "Calculated %s, but %s was expected";
		assertArrayEquals(
				String.format(message, TimeSeriesUtil.toString(zTransformed), TimeSeriesUtil.toString(expectation)),
				expectation, zTransformed, 0.);
	}

}
