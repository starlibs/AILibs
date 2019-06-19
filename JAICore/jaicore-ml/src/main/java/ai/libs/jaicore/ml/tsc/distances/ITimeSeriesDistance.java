package ai.libs.jaicore.ml.tsc.distances;

/**
 * Interface that describes a distance measure of two time series.
 *
 * @author fischor
 */
public interface ITimeSeriesDistance {

	/**
	 * Calculates the distance between two time series.
	 *
	 * @param a First time series.
	 * @param b Second time series.
	 * @return Distance between the first and second time series.
	 */
	public double distance(double[] a, double[] b);
}