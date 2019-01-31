package jaicore.ml.tsc.distances;

import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * Class for the Weighted Dynamic Time Warp Distance Calculation.
 */
public class WeightedDynamicTimeWarping implements ITimeSeriesDistance {

    int p;
    /**
     * Controls the level of penalization for the points with larger phase
     * difference
     */
    double g;
    double Wmax;

    private double[] weights;

    /**
     * Constructor.
     * 
     * @param p    Defines the p-norm.
     * @param g    Controls the penelization in weights for points with larger phase
     *             difference.
     * @param Wmax The desired upper bound for the weight parameter.
     */
    public WeightedDynamicTimeWarping(int p, double g, double Wmax) {
        this.p = p;
        this.g = g;
        this.Wmax = Wmax;
    }

    @Override
    public double distance(double[] A, double[] B) throws IllegalArgumentException {
        // Parameter checks.
        isSameLengthOrException(A, B);

        int n = A.length;
        double[][] M = new double[n + 1][n + 1];

        initWeights(n);

        // Dynamic Programming initialization.
        for (int i = 1; i <= n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int i = 1; i <= n; i++)
            M[0][i] = Double.MAX_VALUE;
        M[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                // Paper: | w[i-j] (a_i - b_j) |^p
                double cost = Math.pow(Math.abs(weights[Math.abs(i - j)] * (A[i - 1] - B[j - 1])), p);
                double mini = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + mini;
            }
        }

        return Math.pow(M[n][n], 1 / (double) p);
    }

    /**
     * Initialize weights, as explained in 4.2 Modified logistic weight function.
     * 
     * @param m Length of the time series, i.e. length of the weight vector.
     */
    private void initWeights(int m) {
        weights = new double[m];
        double mc = (double) m / 2; // center of time series.

        for (int i = 0; i < m; i++) {
            weights[i] = Wmax / (1 + Math.exp(-g * (i - mc)));
        }
    }

}