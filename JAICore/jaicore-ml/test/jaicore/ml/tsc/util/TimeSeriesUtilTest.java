package jaicore.ml.tsc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import junit.framework.Assert;
import weka.core.Instances;

/**
 * Time series util unit tests.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesUtilTest {
	private static final double EPS_DELTA = 0.0000001;

	@Test
	public void timeSeriesDatasetToWekaInstancesTest() {
		final List<INDArray> valueMatrix = Arrays
				.asList(Nd4j.create(new double[][] { { 1, 2, 3, 4 }, { 1, 2, 2, 2 } }));
		TimeSeriesDataset dataset = new TimeSeriesDataset(valueMatrix, new ArrayList<>(),
				Nd4j.create(new double[] { 1, 2 }));

		Instances actResult = WekaUtil.timeSeriesDatasetToWekaInstances(dataset);

		Assert.assertEquals(2, actResult.numInstances());
		Assert.assertEquals(5, actResult.numAttributes()); // 4 + target
		Assert.assertEquals(3, actResult.get(0).value(2), 0.001);
		Assert.assertEquals(2, actResult.get(1).classValue(), 0.001);
	}

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
}
