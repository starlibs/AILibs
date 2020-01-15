package org.api4.java.common.metric;

import java.util.stream.IntStream;

/**
 * Interface that describes a distance measure of two time series that takes the
 * timestamps into account.
 *
 * @author fischor
 */
public interface ITimeseriesDistanceMetric extends IDistanceMetric {

	/**
	 * Calculates the distance between two time series.
	 *
	 * @param a  First time series.
	 * @param tA Timestamps for the first time series.
	 * @param b  Second time series.
	 * @param tB Timestamps for the second times series.
	 * @return Distance between the first and second time series.
	 */
	public double distance(double[] a, double[] tA, double[] b, double[] tB);

	@Override
	public default double distance(final double[] a, final double[] b) {
		return distance(a, IntStream.range(0, a.length).mapToDouble(x -> x).toArray(), b, IntStream.range(0, b.length).mapToDouble(x -> x).toArray());
	}
}