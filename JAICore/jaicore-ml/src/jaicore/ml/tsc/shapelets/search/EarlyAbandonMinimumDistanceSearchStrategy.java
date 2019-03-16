package jaicore.ml.tsc.shapelets.search;

import java.util.List;

import jaicore.ml.tsc.shapelets.Shapelet;
import jaicore.ml.tsc.util.MathUtil;
import jaicore.ml.tsc.util.TimeSeriesUtil;

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
	public double findMinimumDistance(Shapelet shapelet, double[] timeSeries) {
		double length = shapelet.getLength();
		int m = timeSeries.length;

		// Order normalized shapelet values
		final double[] S_prime = shapelet.getData();
		final List<Integer> A = TimeSeriesUtil.sortIndexes(S_prime, false); // descending
		final double[] F = TimeSeriesUtil.zNormalize(TimeSeriesUtil.getInterval(timeSeries, 0, shapelet.getLength()),
				this.useBiasCorrection);

		// Online normalization
		double p = 0;
		double q = 0;
		p = MathUtil.sum(TimeSeriesUtil.getInterval(timeSeries, 0, shapelet.getLength()));
		for (int i = 0; i < length; i++) {
			q += timeSeries[i] * timeSeries[i];
		}

		double b = MathUtil.singleSquaredEuclideanDistance(S_prime, F);

		for (int i = 1; i <= m - length; i++) {

			double t_i = timeSeries[i - 1];
			double t_il = timeSeries[i - 1 + shapelet.getLength()];
			p -= t_i;
			q -= t_i * t_i;
			p += t_il;
			q += t_il * t_il;
			double x_bar = p / length;
			double s = q / (length) - x_bar * x_bar;
			s = s < 0.000000001d ? 0d
					: Math.sqrt((this.useBiasCorrection ? ((double) length / (double) (length - 1d)) : 1d) * s);

			int j = 0;
			double d = 0d;

			// Early abandon
			while (j < length && d < b) {
				final double normVal = (s == 0.0 ? 0d : (timeSeries[i + A.get(j)] - x_bar) / s);
				final double diff = S_prime[A.get(j)] - normVal;

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
