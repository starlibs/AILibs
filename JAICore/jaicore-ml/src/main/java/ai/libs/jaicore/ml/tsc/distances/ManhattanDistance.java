package ai.libs.jaicore.ml.tsc.distances;

import static ai.libs.jaicore.ml.tsc.util.TimeSeriesUtil.isSameLengthOrException;

/**
 * Implementation of the Manhattan distance for time series.
 *
 * The Manhattan distance for two time series <code>A</code> and <code>B</code>
 * of length <code>n</code> is defined as
 * <code>\sum_{i=0}^{n} |A_i - B_i|</code>. Therefore, it is required for
 * <code>A</code> and <code>B</code> to be of the same length.
 *
 * @author fischor
 */
public class ManhattanDistance implements ITimeSeriesDistance {

	@Override
	public double distance(final double[] a, final double[] b) {
		// Parameter checks.
		isSameLengthOrException(a, b);

		int n = a.length;
		// Sum over all elements.
		double result = 0;
		for (int i = 0; i < n; i++) {
			result += Math.abs(a[i] - b[i]);
		}

		return result;
	}

}