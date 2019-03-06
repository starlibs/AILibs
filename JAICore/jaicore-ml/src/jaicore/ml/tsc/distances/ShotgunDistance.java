package jaicore.ml.tsc.distances;

import java.util.Arrays;

import jaicore.ml.tsc.util.TimeSeriesUtil;;

/**
 * ShotgunDistance
 */
public class ShotgunDistance implements ITimeSeriesDistance {

    /**
     * Windw length.
     */
    private int windowLength;

    /**
     * Mean normalization. If false, no mean substraction at vertical alignment.
     */
    private boolean meanNormalization;

    private EuclideanDistance euclideanDistance = new EuclideanDistance();

    public ShotgunDistance(int windowLength, boolean meanNormalization) {
        this.windowLength = windowLength;
        this.meanNormalization = meanNormalization;
    }

    @Override
    public double distance(double[] A, double[] B) {
        // Assure that max(A.length, B.length) <= windowLength, otherwise
        // IndexOutOfBoundsException will be thrown.

        double totalDistance = 0;

        // For each disjoint query window with lenth this.windowLength.
        int numberOfDisjointWindows = A.length / this.windowLength;
        for (int i = 0; i < numberOfDisjointWindows; i++) {
            int startOfDisjointWindow = i * this.windowLength;
            double[] disjointWindow = Arrays.copyOfRange(A, startOfDisjointWindow,
                    startOfDisjointWindow + this.windowLength);

            // Vertical alignment.
            if (this.meanNormalization) {
                disjointWindow = TimeSeriesUtil.zTransform(disjointWindow);
            } else {
                disjointWindow = TimeSeriesUtil.normalizeByStandardDeviation(disjointWindow);
            }

            // Holds the minumum distance between the current disjoint window and all
            // sliding windows.
            double windowDistance = Double.MAX_VALUE;

            // Slide window length windowLength and stride 1 over the time series B.
            int numberOfSlidingWindows = B.length - this.windowLength + 1;
            for (int j = 0; j < numberOfSlidingWindows; j++) {
                int startOfSlidingWindow = j;
                double[] slidingWindow = Arrays.copyOfRange(B, startOfSlidingWindow,
                        startOfSlidingWindow + this.windowLength);

                // Vertical alignment.
                if (this.meanNormalization) {
                    slidingWindow = TimeSeriesUtil.zTransform(slidingWindow);
                } else {
                    slidingWindow = TimeSeriesUtil.normalizeByStandardDeviation(slidingWindow);
                }

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