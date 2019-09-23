package ai.libs.jaicore.ml.tsc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.ml.core.tabular.dataset.attribute.NumericAttribute;
import ai.libs.jaicore.ml.core.timeseries.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.core.timeseries.util.WekaUtil;
import junit.framework.Assert;
import weka.core.Instances;

/**
 * Weka util unit tests.
 *
 * @author Julian Lienen
 *
 */
public class WekaUtilTest {
	/**
	 * See {@link WekaUtil#timeSeriesDatasetToWekaInstances(TimeSeriesDataset)}.
	 */
	@Test
	public void timeSeriesDatasetToWekaInstancesTest() {
		final List<INDArray> valueMatrix = Arrays.asList(Nd4j.create(new double[][] { { 1, 2, 3, 4 }, { 1, 2, 2, 2 } }));

		TimeSeriesDataset<Double> dataset = new TimeSeriesDataset<>(valueMatrix, new ArrayList<>(), Arrays.asList(1.0, 2.0), Arrays.asList(new NumericAttribute("class")));

		Instances actResult = WekaUtil.timeSeriesDatasetToWekaInstances(dataset);

		Assert.assertEquals(2, actResult.numInstances());
		Assert.assertEquals(5, actResult.numAttributes()); // 4 + target
		Assert.assertEquals(3, actResult.get(0).value(2), 0.001);
		Assert.assertEquals(2, actResult.get(1).classValue(), 0.001);
	}
}
