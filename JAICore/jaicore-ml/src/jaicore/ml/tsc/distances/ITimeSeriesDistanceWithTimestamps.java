package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * Interface that describes a distance measure of two time series that takes the
 * timestamps into account.
 */
public interface ITimeSeriesDistanceWithTimestamps extends ITimeSeriesDistance {

    /**
     * Calculates the distance between two time series.
     * 
     * @param A  First time series.
     * @param tA Timestamps for the first time series.
     * @param B  Second time series.
     * @param tB Timestamps for the second times series.
     * @return Distance between the first and second time series.
     */
    public double distance(INDArray A, INDArray tA, INDArray B, INDArray tB) throws TimeSeriesLengthException;
}