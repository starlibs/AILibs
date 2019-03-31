package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Interface that describes a distance measure of two time series that takes the
 * timestamps into account.
 * 
 * @author fischor
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
    public double distance(double[] A, double[] tA, double[] B, double[] tB);

    @Override
    default public double distance(double[] A, double[] B) {
        // Create dummy timestamps for A and B.
        double[] tA = TimeSeriesUtil.createEquidistantTimestamps(A);
        double[] tB = TimeSeriesUtil.createEquidistantTimestamps(B);

        return distance(A, tA, B, tB);
    }
}