package jaicore.ml.tsc.shapelets.search;

import jaicore.ml.tsc.shapelets.Shapelet;
import jaicore.ml.tsc.util.MathUtil;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Class implementing a search strategy used for finding the minimum distance of
 * a {@link Shapelet} object to a time series. The approach uses an exhaustive
 * search as described in the paper 'Jason Lines, Luke M. Davis, Jon Hills, and
 * Anthony Bagnall. 2012. A shapelet transform for time series classification.
 * In Proceedings of the 18th ACM SIGKDD international conference on Knowledge
 * discovery and data mining (KDD '12). ACM, New York, NY, USA, 289-297.'.
 * 
 * @author Julian Lienen
 *
 */
public class ExhaustiveMinimumDistanceSearchStrategy extends AMinimumDistanceSearchStrategy {

	/**
	 * Standard constructor.
	 * 
	 * @param useBiasCorrection
	 *            See {@link AMinimumDistanceSearchStrategy#useBiasCorrection}
	 */
	public ExhaustiveMinimumDistanceSearchStrategy(final boolean useBiasCorrection) {
		super(useBiasCorrection);
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
	@Override
	public double findMinimumDistance(Shapelet shapelet, double[] timeSeries) {
		final int l = shapelet.getLength();
		final int n = timeSeries.length;

		double min = Double.MAX_VALUE;

		double[] normalizedShapeletData = shapelet.getData();

		// TODO: Reference implementation uses i < n-l => Leads sometimes to a better
		// performance => Check this
		for (int i = 0; i <= n - l; i++) {
			double tmpED = MathUtil.singleSquaredEuclideanDistance(normalizedShapeletData,
					TimeSeriesUtil.zNormalize(TimeSeriesUtil.getInterval(timeSeries, i, i + l),
							this.useBiasCorrection));
			if (tmpED < min)
				min = tmpED;
		}
		return min / l;
	}

}
