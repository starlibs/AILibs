package jaicore.ml.tsc.distances;

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
    public double distance(double[] A, double[] B) throws TimeSeriesLengthException;
}