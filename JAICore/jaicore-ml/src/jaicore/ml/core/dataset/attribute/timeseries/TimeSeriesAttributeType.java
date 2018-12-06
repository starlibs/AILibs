package jaicore.ml.core.dataset.attribute.timeseries;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * Describes a time series type as an 1-NDArray with a fixed length.
 */
public class TimeSeriesAttributeType implements ITimeSeriesAttributeType {

    private final int length;

    public TimeSeriesAttributeType(int length) {
        this.length = length;
    }

    /**
     * Validates whether a INDArray conforms to this time series. An INDArray
     * confirms to this value, if its rank is 1 and its length equals the length of
     * this time series.
     *
     * @param value The value to validated.
     * @return Returns true if the given value conforms
     */
    @Override
    public boolean isValidValue(INDArray value) {
        return value.rank() == 1 && value.length() == this.length;
    }

    @Override
    public IAttributeValue<INDArray> buildAttributeValue(Object value) {
        if (value instanceof INDArray) {
            return new TimeSeriesAttributeValue(this, (INDArray) value);
        } else {
            throw new IllegalArgumentException("Value has to be an INDArray");
        }
    }

    @Override
    public IAttributeValue<INDArray> buildAttributeValue(String stringDescription) {
        double[] data = Stream.of(stringDescription.split(",")).mapToDouble(Double::parseDouble).toArray();
        int[] shape = { data.length };
        INDArray value = Nd4j.create(data, shape);
        return this.buildAttributeValue(value);
    }

    @Override
    public int getLength() {
        return length;
    }

}