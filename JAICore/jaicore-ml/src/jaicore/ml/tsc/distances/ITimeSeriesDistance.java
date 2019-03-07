package jaicore.ml.tsc.distances;

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
    public double distance(double[] A, double[] B);
}