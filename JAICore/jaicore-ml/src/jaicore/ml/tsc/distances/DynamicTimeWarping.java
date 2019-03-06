package jaicore.ml.tsc.distances;

/**
 * Implementation of the Dynamic Time Warping (DTW) measure as published in
 * "Using DynamicTimeWarpingto FindPatterns in Time Series" Donald J. Berndt and
 * James Clifford.
 * 
 * In DTW the time series are "warped" non-linearly in the time dimension to
 * determine a measure of their similarity independent of certain non-linear
 * variations in the time dimension.
 * 
 * Given two time series <code>A</code> and <code>B</code> the dynamic
 * programming formulation is based on the following recurrent definition:
 * <code>gamma(i,j) = delta(i,j) + min {gamma(i-1, j), gamma(i-1,j-1), gamma(i, j-1)}</code>
 * where <code>gamma(i,j)</code> is the cummulative distance up to
 * <code>i,j</code> and
 * <code>delta(i,j) is the point distance between <code>A_i</code> and
 * <code>B_i</code>.
 */
public class DynamicTimeWarping implements ITimeSeriesDistance {

    /** Distance measure for scalar points. */
    IScalarDistance delta;

    /**
     * Creates an instance with absolute distance as point distance.
     */
    public DynamicTimeWarping() {
        this((x, y) -> Math.abs(x - y));
    }

    /**
     * Creates an instance with a given scalar distance measure.
     * 
     * @param delta Scalar distance measure.
     */
    public DynamicTimeWarping(IScalarDistance delta) {
        // Parameter checks.
        if (delta == null)
            throw new IllegalArgumentException("Parameter delta must not be null.");

        this.delta = delta;
    }

    @Override
    public double distance(double[] A, double[] B) {
        // Care in the most algorithm descriptions, the time series are 1-indexed.

        int n = A.length;
        int m = B.length;
        double[][] M = new double[n + 1][m + 1]; // from 0 to n+1 incl. and 0 to m+1 incl.

        // Initialize first row and column to infinity (except [0][0]).
        for (int i = 1; i <= n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int j = 1; j <= m; j++)
            M[0][j] = Double.MAX_VALUE;
        // Initialize [0][0] with 0.
        M[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = delta.distance(A[i - 1], B[j - 1]); // 1 indexed in algo.
                double mini = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + mini;
            }
        }

        return M[n][m];
    }

    public double distanceWithWindow(double[] A, double[] B, int w) {
        int n = A.length;
        int m = B.length;
        double[][] M = new double[n + 1][m + 1];

        w = Math.max(w, Math.abs(n - m));

        // Initialize first row and column to infinity (except [0][0]).
        for (int i = 1; i <= n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int j = 1; j <= m; j++)
            M[0][j] = Double.MAX_VALUE;
        // Initialize [0][0] with 0.
        M[0][0] = 0d;

        for (int i = 1; i <= n; i++) {
            for (int j = Math.max(1, i - w); j <= Math.min(m, i + w); j++) {
                double cost = delta.distance(A[i - 1], B[j - 1]);
                double mini = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + mini;
            }
        }

        return M[n][m];
    }

}