package jaicore.ml.core.dataset.attribute.timeseries;

import org.nd4j.linalg.api.ndarray.INDArray;
import jaicore.ml.core.dataset.attribute.AAttributeValue;

/**
 * Represents a time series attribute value, as it can be part of a
 * {@link jaicore.ml.core.dataset.IInstance}
 */
public class TimeSeriesAttributeValue extends AAttributeValue<INDArray> {

    public TimeSeriesAttributeValue(final ITimeSeriesAttributeType type) {
        super(type);
    }

    public TimeSeriesAttributeValue(final ITimeSeriesAttributeType type, final INDArray value) {
        super(type, value);
    }

}