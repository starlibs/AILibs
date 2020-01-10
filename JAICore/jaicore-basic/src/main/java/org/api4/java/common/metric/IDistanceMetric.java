package org.api4.java.common.metric;

import org.api4.java.common.math.IMetric;

/**
 * Interface that describes a distance measure of two time series.
 *
 * @author fischor
 */
public interface IDistanceMetric extends IMetric<double[]> {

	/**
	 * Calculates the distance between two time series.
	 *
	 * @param a First time series.
	 * @param b Second time series.
	 * @return Distance between the first and second time series.
	 */
	public double distance(double[] a, double[] b);

	@Override
	default double getDistance(final double[] a, final double[] b) {
		return this.distance(a, b);
	}
}