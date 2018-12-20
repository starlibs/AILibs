package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * Interface that describes a distance measure of two time series.
 */
public interface IDistance {

    /**
     * Calculates the distance between two time series.
     * 
     * @param timeSeries1 First time series.
     * @param timeSeries2 Second time series.
     * @return Distance between the first and second time series.
     */
    public double distance(INDArray A, INDArray tA, INDArray B, INDArray tB) throws TimeSeriesLengthException;
}