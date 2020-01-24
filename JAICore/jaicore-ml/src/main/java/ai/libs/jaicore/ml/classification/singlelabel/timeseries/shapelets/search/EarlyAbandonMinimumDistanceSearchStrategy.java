package ai.libs.jaicore.ml.classification.singlelabel.timeseries.shapelets.search;

import java.util.List;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.shapelets.Shapelet;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.util.MathUtil;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.util.TimeSeriesUtil;

/**
 * Class implementing a search strategy used for finding the minimum distance of
 * a {@link Shapelet} object to a time series. The approach uses early
 * abandoning as described in algorithm 2 in the paper 'Jason Lines, Luke M.
 * Davis, Jon Hills, and Anthony Bagnall. 2012. A shapelet transform for time
 * series classification. In Proceedings of the 18th ACM SIGKDD international
 * conference on Knowledge discovery and data mining (KDD '12). ACM, New York,
 * NY, USA, 289-297.'.
 *
 * @author Julian Lienen
 *
 */
public class EarlyAbandonMinimumDistanceSearchStrategy extends AMinimumDistanceSearchStrategy {
	/**
	 * Standard constructor.
	 *
	 * @param useBiasCorrection
	 *            See {@link AMinimumDistanceSearchStrategy#useBiasCorrection}
	 */
	public EarlyAbandonMinimumDistanceSearchStrategy(final boolean useBiasCorrection) {
		super(useBiasCorrection);
	}

	/**
	 * Optimized function returning the minimum distance among all subsequences of
	 * the given <code>timeSeries</code> to the <code>shapelet</code>'s data. This
	 * function implements the algorithm 2 mentioned in the original paper. It
	 * performs the similarity search with online normalization and early abandon.
	 *
	 * @param shapelet
	 *            The shapelet to be compared to all subsequences
	 * @param timeSeries
	 *            The time series which subsequences are compared to the shapelet's
	 *            data
	 * @return Return the minimum distance among all subsequences
	 */
	@Override
	public double findMinimumDistance(final Shapelet shapelet, final double[] timeSeries) {
		double length = shapelet.getLength();
		int m = timeSeries.length;

		// Order normalized shapelet values
		final double[] sPrimeVector = shapelet.getData();
		final List<Integer> aVector = TimeSeriesUtil.sortIndexes(sPrimeVector, false); // descending
		final double[] fVector = TimeSeriesUtil.zNormalize(TimeSeriesUtil.getInterval(timeSeries, 0, shapelet.getLength()), this.useBiasCorrection);

		// Online normalization
		double p = 0;
		double q = 0;
		p = MathUtil.sum(TimeSeriesUtil.getInterval(timeSeries, 0, shapelet.getLength()));
		for (int i = 0; i < length; i++) {
			q += timeSeries[i] * timeSeries[i];
		}

		double b = MathUtil.singleSquaredEuclideanDistance(sPrimeVector, fVector);

		for (int i = 1; i <= m - length; i++) {

			double ti = timeSeries[i - 1];
			double til = timeSeries[i - 1 + shapelet.getLength()];
			p -= ti;
			q -= ti * ti;
			p += til;
			q += til * til;
			double xBar = p / length;
			double s = q / (length) - xBar * xBar;

			if (s < 0.000000001d) {
				s = 0d;
			} else {
				s = Math.sqrt((this.useBiasCorrection ? (length / (length - 1d)) : 1d) * s);
			}

			int j = 0;
			double d = 0d;

			// Early abandon
			while (j < length && d < b) {
				final double normVal = (s == 0.0 ? 0d : (timeSeries[i + aVector.get(j)] - xBar) / s);
				final double diff = sPrimeVector[aVector.get(j)] - normVal;

				d += diff * diff;
				j++;
			}

			if (j == length && d < b) {
				b = d;
			}
		}

		return b / length;

	}

}
