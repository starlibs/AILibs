package ai.libs.jaicore.ml.tsc.distances;

import ai.libs.jaicore.ml.tsc.util.TimeSeriesUtil;

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
	 * @param a  First time series.
	 * @param tA Timestamps for the first time series.
	 * @param b  Second time series.
	 * @param tB Timestamps for the second times series.
	 * @return Distance between the first and second time series.
	 */
	public double distance(double[] a, double[] tA, double[] b, double[] tB);

	@Override
	public default double distance(final double[] a, final double[] b) {
		// Create dummy timestamps for A and B.
		double[] tA = TimeSeriesUtil.createEquidistantTimestamps(a);
		double[] tB = TimeSeriesUtil.createEquidistantTimestamps(b);

		return distance(a, tA, b, tB);
	}
}