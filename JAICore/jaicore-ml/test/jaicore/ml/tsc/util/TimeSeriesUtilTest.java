package jaicore.ml.tsc.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import junit.framework.Assert;

/**
 * Time series util unit tests.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesUtilTest {
	/**
	 * Epsilon delta used for double comparisons.
	 */
	private static final double EPS_DELTA = 0.0001;

	/**
	 * See {@link TimeSeriesUtil#normalizeINDArray(INDArray, boolean)}.
	 */
	@Test
	public void normalizeINDArrayTest() {
		INDArray testArray = Nd4j.create(new double[] { 1, 2, 3 });

		Assert.assertTrue(Nd4j.create(new double[] { -1, 0, 1 })
				.equalsWithEps(TimeSeriesUtil.normalizeINDArray(testArray, false), EPS_DELTA));
	}

	/**
	 * See {@link TimeSeriesUtil#sortIndexes(double[], boolean)}.
	 */
	@Test
	public void sortIndexesTest() {
		double[] vector = new double[] { 4, 2, 6 };
		double[] vector2 = new double[] { 2, 4, 6 };

		List<Integer> result1 = TimeSeriesUtil.sortIndexes(vector, true);
		List<Integer> result1Inv = TimeSeriesUtil.sortIndexes(vector, false);

		Assert.assertEquals(Arrays.asList(1, 0, 2), result1);
		Assert.assertEquals(Arrays.asList(2, 0, 1), result1Inv);

		List<Integer> result2 = TimeSeriesUtil.sortIndexes(vector2, true);

		Assert.assertEquals(Arrays.asList(0, 1, 2), result2);

	}

	/**
	 * See {@link TimeSeriesUtil#getMode(int[])}.
	 */
	@Test
	public void getModeTest() {
		int[] testArray = new int[] { 1, 2, 1, 1, 4, 6, 6, 6, 7, 7, 7, 7, 7, 7, 2, 1, 1 };
		Assert.assertEquals(7, TimeSeriesUtil.getMode(testArray));

		testArray = new int[] {};
		Assert.assertEquals(-1, TimeSeriesUtil.getMode(testArray));

		testArray = new int[] { 1, 1, 2, 2 };
		Assert.assertEquals(1, TimeSeriesUtil.getMode(testArray));
  }

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
