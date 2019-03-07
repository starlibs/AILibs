package jaicore.ml.tsc.distances;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Dynamic Time Warping (DTW) measure as published in
 * "Weighted dynamic time warping for time series classification" by Young-Seon
 * Jeong, Myong K. Jeong and Olufemi A. Omitaomu.
 * 
 * DTW does not account for the relative importance regarding the phase
 * difference between a reference point and a testing point. This may lead to
 * misclassification especially in applications where the shape similarity
 * between two sequences is a major consideration for an accurate recognition.
 * Therefore, [the authors] propose a novel distance measure, called a weighted
 * DTW (WDTW), which is a penalty-based DTW. [Their] approach penalizes points
 * with higher phase difference between a reference point and a testing point in
 * order to prevent minimum distance distortion caused by outliers.
 */
public class WeightedDynamicTimeWarping implements ITimeSeriesDistance {

    /**
     * Controls the level of penalization for the points with larger phase
     * difference.
     */
    private double g;

    /**
     * The desired upper bound for the weight parameter that is used to penalize
     * points with higher phase difference.
     */
    private double maximumWeight;

    /** Distance measure for scalar points. */
    private IScalarDistance d;

    /** Memorizes the calculated weight vectors for a specific length. */
    private Map<Integer, double[]> weightMemoization = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param g             Controls the penelization in weights for points with
     *                      larger phase difference.
     * @param maximumWeight The desired upper bound for the weight parameter that is
     *                      used to penalize points with higher phase difference.
     */
    public WeightedDynamicTimeWarping(double g, double maximumWeight, IScalarDistance d) {
        this.g = g;
        this.maximumWeight = maximumWeight;
        this.d = d;
    }

    @Override
    public double distance(double[] A, double[] B) {
        int n = A.length;
        int m = B.length;
        double[][] M = new double[n + 1][m + 1];

        double[] weights = calculateWeights(Math.max(n, m));

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
                double cost = weights[Math.abs(i - j)] * this.d.distance(A[i - 1], B[j - 1]);
                double minimum = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + minimum;
            }
        }
        return M[n][m];
    }

    /**
     * Calculates the weight vector via the Modified logistic weight function (see
     * paper 4.2). Uses memoization to avoid multiple calculations for the same
     * length.
     * 
     * @param length Length of the time series, i.e. length of the weight vector. Is
     *               guaranteed to be greater 0 within this class.
     * @return Resulting weight vector.
     */
    protected double[] calculateWeights(int length) {
        // Use memoization.
        double[] memoized = this.weightMemoization.get(length);
        if (memoized != null)
            return memoized;

        // Calculate weights when not memoized.
        double[] weights = new double[length];
        double halfLength = (double) length / 2; // center of time series.

        for (int i = 0; i < length; i++) {
            weights[i] = maximumWeight / (1 + Math.exp(-this.g * (i - halfLength)));
        }

        // Add to memoization-
        this.weightMemoization.put(length, weights);

        return weights;
    }

}