package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * Interface that describes a distance measure of two time series.
 */
public interface ITimeSeriesDistance {

    /**
     * Calculates the distance between two time series.
     * 
     * @param A First time series.
     * @param B Second time series.
     * @return Distance between the first and second time series.
     */
    public double distance(INDArray A, INDArray B) throws TimeSeriesLengthException;
}