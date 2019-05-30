package jaicore.ml.tsc.distances;

import java.util.Arrays;

import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Implementation of Shotgun Distance measure as published in "Towards Time
 * Series Classfication without Human Preprocessing" by Patrick Schäfer (2014).
 *
 * To make many of the standard methods to calculate the distance betwenn two
 * time series applicable, a lot of time and effort has to be spent by a domain
 * expert to filter the data and extrdact equal-length, equal-scale and alighned
 * patterns. The Shotgun Distance avoids preprocessing the data for alignment,
 * scaling or length. This is achieved by breaking the query time series into
 * disjoint windows (subsequences) of fixed length. These windows are slid along
 * the sample time series (with stride 1) to find the best matching position in
 * terms of minimizing a distance metric (e.g. Euclidean distance or DTW
 * distance).
 *
 * @author fischor
 */
public class ShotgunDistance implements ITimeSeriesDistance {

	/**
	 * The window length.
	 */
	private int windowLength;

	/**
	 * Mean normalization. If false, no mean substraction at vertical alignment of
	 * windows.
	 */
	private boolean meanNormalization;

	/**
	 * The euclidean distance used to calculate the distance between different
	 * windows.
	 */
	private EuclideanDistance euclideanDistance = new EuclideanDistance();

	/**
	 * Constructor for the Shotgun Distance.
	 *
	 * @param windowLength The window length.
	 * @param meanNormalization Mean normalization. If false, no mean substraction
	 *            at vertical alignment of windows.
	 */
	public ShotgunDistance(final int windowLength, final boolean meanNormalization) {
		// Parameter checks.
		if (windowLength <= 0) {
			throw new IllegalArgumentException("The window length must not be less or equal to zero.");
		}

		this.windowLength = windowLength;
		this.meanNormalization = meanNormalization;
	}

	@Override
	public double distance(final double[] a, final double[] b) {
		// Assure that max(A.length, B.length) <= windowLength, otherwise
		// the result is undefined.

		double totalDistance = 0;

		// For each disjoint query window with lenth this.windowLength.
		int numberOfDisjointWindows = a.length / this.windowLength;
		for (int i = 0; i < numberOfDisjointWindows; i++) {
			int startOfDisjointWindow = i * this.windowLength;
			double[] disjointWindow = Arrays.copyOfRange(a, startOfDisjointWindow, startOfDisjointWindow + this.windowLength);

			// Vertical alignment.
			if (this.meanNormalization) {
				disjointWindow = TimeSeriesUtil.zTransform(disjointWindow);
			} else {
				disjointWindow = TimeSeriesUtil.normalizeByStandardDeviation(disjointWindow);
			}

			// Holds the minumum distance between the current disjoint window and all
			// sliding windows.
			double windowDistance = Double.MAX_VALUE;

			// Slide window with length windowLength and stride 1 over the time series B.
			int numberOfSlidingWindows = b.length - this.windowLength + 1;
			for (int j = 0; j < numberOfSlidingWindows; j++) {
				int startOfSlidingWindow = j;
				double[] slidingWindow = Arrays.copyOfRange(b, startOfSlidingWindow, startOfSlidingWindow + this.windowLength);

				// Vertical alignment.
				if (this.meanNormalization) {
					slidingWindow = TimeSeriesUtil.zTransform(slidingWindow);
				} else {
					slidingWindow = TimeSeriesUtil.normalizeByStandardDeviation(slidingWindow);
				}

				// Calculate distance between disjoint and sliding window. For each disjoint
				// window, keep the minumum distance to all sliding windows.
				double distanceDisjointSliding = this.euclideanDistance.distance(disjointWindow, slidingWindow);
				if (distanceDisjointSliding < windowDistance) {
					windowDistance = distanceDisjointSliding;
				}
			}
			// Aggregate the distance for all disjoint windows to the total distance.
			totalDistance += windowDistance;
		}

		return totalDistance;
	}

	/**
	 * Sets the window length.
	 *
	 * @param windowLength @see #windowLength
	 */
	public void setWindowLength(final int windowLength) {
		this.windowLength = windowLength;
	}

}