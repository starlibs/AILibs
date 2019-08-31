package ai.libs.jaicore.basic.metric;

import org.api4.java.common.metric.IDistanceMetric;

import ai.libs.jaicore.basic.RobustnessUtil;

/**
 * Implementation of the Euclidean distance for time series.
 *
 * The Euclidean distance for two time series <code>A</code> and <code>B</code>
 * of length <code>n</code> is defined as
 * <code>\sqrt{\sum_{i=0}^{n} (A_i - B_i)^2 }</code>. Therefore, it is required
 * for <code>A</code> and <code>B</code> to be of the same length.
 *
 * @author fischor
 */
public class EuclideanDistance implements IDistanceMetric {

	@Override
	public double distance(final double[] a, final double[] b) {
		// Parameter checks.
		RobustnessUtil.sameLengthOrDie(a, b);

		int n = a.length;
		// Sum over all elements.
		double result = 0;
		for (int i = 0; i < n; i++) {
			result += Math.pow((a[i] - b[i]), 2);
		}
		result = Math.sqrt(result);

		return result;
	}

}