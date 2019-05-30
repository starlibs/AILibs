package jaicore.ml.core.dataset.attribute.timeseries;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * Unit test cases for the {@link TimeSeriesAttributeType} class.
 */
public class TimeSeriesAttributeTypeTest {

    @Test
    public void testValidFloatNDArrayGetsValidated() {
        int length = 6;
        int[] shape = { length };
        float[] data = { 1, 1, 1, 1, 1, 1 };
        INDArray array = Nd4j.create(data, shape);
        TimeSeriesAttributeType type = new TimeSeriesAttributeType(length);
        assertTrue(type.isValidValue(array));
    }

    @Test
    public void testValidDoubleNDArrayGetsValidated() {
        int length = 6;
        int[] shape = { length };
        double[] data = { 1, 1, 1, 1, 1, 1 };
        INDArray array = Nd4j.create(data, shape);
        TimeSeriesAttributeType type = new TimeSeriesAttributeType(length);
        assertTrue(type.isValidValue(array));
    }

    @Test
    public void testBuildAttributeValueFromStringDescription() {
        // Input
        int length = 6;
        String str = "1.6,1.77,1.9,1.6,1.7,1.7";
        // Expectation
        int[] eShape = { length };
        double[] eData = { 1.6, 1.77, 1.9, 1.6, 1.7, 1.7 };
        INDArray eArray = Nd4j.create(eData, eShape);
        // Build attribute value.
        TimeSeriesAttributeType type = new TimeSeriesAttributeType(length);
        IAttributeValue<INDArray> value = type.buildAttributeValue(str);
        // Test equality.
        assertTrue(ArrayUtil.equals(value.getValue().data().asFloat(), eArray.data().asDouble()));
    }

    @Test
    public void testBuildAttributeValueFromObject() {
        // Expectation
        int length = 6;
        int[] eShape = { length };
        double[] eData = { 1.6, 1.77, 1.9, 1.6, 1.7, 1.7 };
        INDArray eArray = Nd4j.create(eData, eShape);
        // Build attribute value.
        TimeSeriesAttributeType type = new TimeSeriesAttributeType(length);
        IAttributeValue<INDArray> value = type.buildAttributeValue(eArray);
        // test equality.
        assertTrue(ArrayUtil.equals(value.getValue().data().asFloat(), eArray.data().asDouble()));
    }

}