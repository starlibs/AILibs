package jaicore.ml.core.dataset.attribute.timeseries;

import org.nd4j.linalg.api.ndarray.INDArray;
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
        return null;
    }

    @Override
    public IAttributeValue<INDArray> buildAttributeValue(String stringDescription) {
        return null;
    }

    @Override
    public int getLength() {
        return length;
    }

}