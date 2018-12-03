package jaicore.ml.core.dataset.attribute.timeseries;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

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

}