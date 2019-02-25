package jaicore.ml.tsc.distances;

import java.util.Arrays;

import jaicore.ml.tsc.util.TimeSeriesUtil;;

/**
 * ShotgunDistance
 */
public class ShotgunDistance implements ITimeSeriesDistance {

    private int windowLength;
    private boolean meanNormalization;

    private EuclideanDistance euclideanDistance = new EuclideanDistance();

    public ShotgunDistance(int windowLength, boolean meanNormalization) {
        this.windowLength = windowLength;
        this.meanNormalization = meanNormalization;
    }

    @Override
    public double distance(double[] A, double[] B) {
        int totalDistance = 0;

        // For each disjoint query window with lenth windowLength.
        int numberOfDisjointWindows = A.length / windowLength;
        for (int i = 0; i < numberOfDisjointWindows; i++) {
            int startOfDisjointWindow = i * windowLength;
            double[] disjointWindow = Arrays.copyOfRange(A, startOfDisjointWindow,
                    startOfDisjointWindow + windowLength);
            disjointWindow = TimeSeriesUtil.zTransform(disjointWindow); // TODO: Optional mean substraction.

            // Holds the minumum distance between the current disjoint window and all
            // sliding windows.
            double windowDistance = Double.MAX_VALUE;

            // Slide window length windowLength and stride 1 over the time series B.
            int numberOfSlidingWindows = B.length - windowLength + 1;
            for (int j = 0; j < numberOfSlidingWindows; j++) {
                int startOfSlidingWindow = j;
                double[] slidingWindow = Arrays.copyOfRange(B, startOfSlidingWindow,
                        startOfSlidingWindow + windowLength);
                slidingWindow = TimeSeriesUtil.zTransform(slidingWindow);

                // Calculate distance between disjoint and sliding window. For each disjoint
                // window, keep the minumum distance to all sliding windows.
                double distanceDisjointSliding = euclideanDistance.distance(disjointWindow, slidingWindow); 
                if (distanceDisjointSliding < windowDistance) {
                    windowDistance = distanceDisjointSliding;
                }
            }
            // Aggregate the distance for all disjoint windows to the total distance.
            totalDistance += windowDistance;
        }

        return totalDistance;
    }

}