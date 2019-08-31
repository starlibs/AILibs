package ai.libs.jaicore.basic.complexity;

import org.api4.java.common.timeseries.ITimeSeriesComplexity;

/**
 * Complexity metric as described in "A Complexity-Invariant Distance Measure
 * for Time Series".
 *
 * $$ c = sum_{i=1}^n-1 \sqrt{ (T_i - T_{i+1})^2 }$$
 *
 * where $T_i$ are the values of the time series.
 *
 * @author fischor
 */
public class SquaredBackwardDifferenceComplexity implements ITimeSeriesComplexity {

	@Override
	public double complexity(final double[] t) {
		int n = t.length;
		double sum = .0;
		for (int i = 0; i < n - 1; i++) {
			sum += (t[i] - t[i + 1]) * (t[i] - t[i + 1]);
		}
		return Math.sqrt(sum);
	}

}