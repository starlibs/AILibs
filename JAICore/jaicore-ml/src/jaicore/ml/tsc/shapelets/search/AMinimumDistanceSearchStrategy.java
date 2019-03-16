package jaicore.ml.tsc.shapelets.search;

import jaicore.ml.tsc.shapelets.Shapelet;

/**
 * Abstract class for minimum distance search strategies. Subclasses implement
 * functionality to find the minimum distance between a given {@link Shapelet}
 * object and a time series.
 * 
 * @author Julian Lienen
 *
 */
public abstract class AMinimumDistanceSearchStrategy {
	/**
	 * Indicator whether Bessel's correction should be used within any distance
	 * calculation;
	 */
	protected boolean useBiasCorrection;

	/**
	 * Constructor.
	 * 
	 * @param useBiasCorrection
	 *            See {@link AMinimumDistanceSearchStrategy#useBiasCorrection}
	 */
	public AMinimumDistanceSearchStrategy(final boolean useBiasCorrection) {
		this.useBiasCorrection = useBiasCorrection;
	}

	/**
	 * Function returning the minimum distance among all subsequences of the given
	 * <code>timeSeries</code> to the <code>shapelet</code>'s data.
	 * 
	 * @param shapelet
	 *            The shapelet to be compared to all subsequences
	 * @param timeSeries
	 *            The time series which subsequences are compared to the shapelet's
	 *            data
	 * @return Return the minimum distance among all subsequences
	 */
	public abstract double findMinimumDistance(final Shapelet shapelet, final double[] timeSeries);
}
