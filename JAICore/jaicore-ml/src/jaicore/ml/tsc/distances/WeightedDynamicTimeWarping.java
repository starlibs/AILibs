package jaicore.ml.tsc.distances;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for the Weighted Dynamic Time Warp Distance Calculation.
 */
public class WeightedDynamicTimeWarping implements ITimeSeriesDistance {

    int p;

    /**
     * Controls the level of penalization for the points with larger phase
     * difference.
     */
    double g;

    double maximumWeight;

    private Map<Integer, double[]> weightMemoization = new HashMap<>();

    private double[] weights;

    private IScalarDistance d;

    /**
     * Constructor.
     * 
     * @param p             Defines the p-norm.
     * @param g             Controls the penelization in weights for points with
     *                      larger phase difference.
     * @param maximumWeight The desired upper bound for the weight parameter.
     */
    public WeightedDynamicTimeWarping(int p, double g, double maximumWeight, IScalarDistance d) {
        this.p = p;
        this.g = g;
        this.maximumWeight = maximumWeight;
        this.d = d;
    }

    @Override
    public double distance(double[] A, double[] B) {
        int n = A.length;
        int m = B.length;
        double[][] M = new double[n + 1][m + 1];

        weights = calculateWeights(Math.max(n, m));

        // Dynamic Programming initialization.
        for (int i = 1; i <= n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int j = 1; j <= m; j++)
            M[0][j] = Double.MAX_VALUE;
        M[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                // Paper: | w[i-j] (a_i - b_j) |^p
                // double cost = Math.pow(Math.abs(weights[Math.abs(i - j)] * (A[i - 1] - B[j -
                // 1])), p);
                double cost = weights[Math.abs(i - j)] * d.distance(A[i - 1], B[j - 1]);
                double minimum = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + minimum;
            }
        }
        return M[n][m];
    }

    /**
     * Initialize weights, as explained in 4.2 Modified logistic weight function.
     * 
     * @param length Length of the time series, i.e. length of the weight vector.
     */
    protected double[] calculateWeights(int length) {
        // Use memoization.
        double[] memoized = weightMemoization.get(length);
        if (memoized != null)
            return memoized;

        // Calculate weights when not memoized.
        double[] weights = new double[length];
        double halfLength = (double) length / 2; // center of time series.

        for (int i = 0; i < length; i++) {
            weights[i] = maximumWeight / (1 + Math.exp(-g * (i - halfLength)));
        }

        // Add to memoization-
        weightMemoization.put(length, weights);

        return weights;
    }

}