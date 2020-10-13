package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.attribute.NDArrayTimeseriesAttribute;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.attribute.NDArrayTimeseriesAttributeValue;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.model.NDArrayTimeseries;

/**
 * Unit test cases for the {@link NDArrayTimeseriesAttribute} class.
 */
public class TimeSeriesAttributeTest {
	private static final String ATTRIBUTE_NAME = "ts";

	@Test
	public void testValidFloatNDArrayGetsValidated() {
		int length = 6;
		int[] shape = { length };
		float[] data = { 1, 1, 1, 1, 1, 1 };
		INDArray array = Nd4j.create(data, shape);
		NDArrayTimeseriesAttribute type = new NDArrayTimeseriesAttribute(ATTRIBUTE_NAME, length);
		assertTrue(type.isValidValue(new NDArrayTimeseriesAttributeValue(type, new NDArrayTimeseries(array))));
	}

	@Test
	public void testValidDoubleNDArrayGetsValidated() {
		int length = 6;
		int[] shape = { length };
		double[] data = { 1, 1, 1, 1, 1, 1 };
		INDArray array = Nd4j.create(data, shape);
		NDArrayTimeseriesAttribute type = new NDArrayTimeseriesAttribute(ATTRIBUTE_NAME, length);
		assertTrue(type.isValidValue(new NDArrayTimeseriesAttributeValue(type, new NDArrayTimeseries(array))));
	}

}