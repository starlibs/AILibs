package ai.libs.jaicore.basic.metric;

import org.api4.java.common.metric.IDistanceMetric;

/**
 * Implementation of the Move-Split-Merge (MSM) measure as published in "The
 * Move-Split-Merge Metric for Time Series" by Alexandra Stefan, Vassilis
 * Athitsos and Gautam Das (2013).
 * 
 * The idea behind the MSM metric is to define a set of operations that can be
 * used to transform any time series into any other.
 * <ul>
 * <li>A <i>Move</i> operation changes the value of a single point of the time
 * series.</li>
 * <li>A <i>Split</i> operation splits a single point of the time series into
 * two consecutive points that have the same value as the original point.</li>
 * <li>A <i>Merge</i> operation merges two consecutive points that have the same
 * value int a single point that has that value.</li>
 * </ul>
 * Each operation has an associated cost. The cost for a <i>Move</i> for
 * <code>x</code> to <code>y</code> is <code>|x-y|</code>. The cost for a
 * <i>Split</i> and <i>Merge</i> is defined by a constant <code>c</code>.
 * 
 * Let <code>S = (s_1, s_2, .., s_n)</code> be a sequence of Move/Split/Merge
 * operations with <code>s_i</code> either Move, Split or a Merge. The
 * Move-Split-Merge distance between to time series <code>A</code> and
 * <code>B</code> is defined be the cost of the lowest-cost transformation
 * <code>S*</code>, such that <code>transform(S*, A) = B</code>.
 * 
 * @author fischor
 */
public class MoveSplitMerge implements IDistanceMetric {

    /** The constant cost for <i>Split</i> and <i>Merge</i> operations. */
    private double c;

    /**
     * Constructor.
     * 
     * @param c The constant cost for <i>Split</i> and <i>Merge</i> operations.
     */
    public MoveSplitMerge(double c) {
        this.c = c;
    }

    @Override
    public double distance(double[] a, double[] b) {
        int n = a.length;
        int m = b.length;
        double[][] cost = new double[n][m];

        // Initialization.
        cost[0][0] = Math.abs(a[0] - b[0]);
        for (int i = 1; i < n; i++) {
            cost[i][0] = cost[i - 1][0] + c(a[i], a[i - 1], b[0]);
        }
        for (int j = 1; j < m; j++) {
            cost[0][j] = cost[0][j - 1] + c(b[j], a[0], b[j - 1]);
        }

        // Dynamic programming.
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                double costMove = cost[i - 1][j - 1] + Math.abs(a[i] - b[j]);
                double cost2 = cost[i - 1][j] + c(a[i], a[i - 1], b[j]);
                double cost3 = cost[i][j - 1] + c(b[j], a[i], b[j - 1]);
                cost[i][j] = Math.min(costMove, Math.min(cost2, cost3));
            }
        }

        return cost[n - 1][m - 1];
    }

    /**
     * Functon C as defined in Equation 9 of the paper.
     * 
     * @param a       The point <code>a_i</code> of the time series <code>A</code>.
     * @param aBefore The point <code>a_{i-1}</code> of the time series
     *                <code>A</code>.
     * @param b       The point <code>b_j</code> of the time series <code>B</code>.
     */
    private double c(double a, double aBefore, double b) {
        if ((aBefore <= a && a <= b) || (aBefore >= a && a >= b)) {
            // x_{i-1} <= x_i <= y_j or x_{i-1} >= x_i >= y_j
            return c;
        } else {
            return c + Math.min(Math.abs(a - aBefore), Math.abs(a - b));
        }
    }

}