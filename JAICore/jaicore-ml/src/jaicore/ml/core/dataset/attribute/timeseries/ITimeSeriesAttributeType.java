package jaicore.ml.core.dataset.attribute.timeseries;

import org.nd4j.linalg.api.ndarray.INDArray;
import jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * Interface for the time series attribute types.
 */
public interface ITimeSeriesAttributeType extends IAttributeType<INDArray> {

    /**
     * @return The length respec. the number of datapoints of this time series
     *         attribute.
     */
    public int getLength();

}