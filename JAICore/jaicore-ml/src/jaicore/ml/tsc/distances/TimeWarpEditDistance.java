package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.util.ScalarDistanceUtil;

import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * TimeWarpEditDistance
 */
public class TimeWarpEditDistance implements ITimeSeriesDistanceWithTimestamps {

    /** Stiffness parameter. */
    private double nu;

    /** Additional cost parameter for deletion. */
    private double lambda;

    /**
     * Distance mesaure used for point distance calculation.
     */
    private IScalarDistance d;

    /**
     * Constructor.
     * 
     * @param lambda Additional cost parameter for deletion.
     * @param nu     Stiffness parameter.
     * @param d      Distance mesaure used for point distance calculation.
     */
    public TimeWarpEditDistance(double lambda, double nu, IScalarDistance d) {
        this.lambda = lambda;
        this.nu = nu;
        this.d = d;
    }

    /**
     * Creates a TimeWarpEditDistance with squared distance as point distance.
     * 
     * @param lambda Additional cost parameter for deletion.
     * @param nu     Stiffness parameter.
     */
    public TimeWarpEditDistance(double lambda, double nu) {
        this(lambda, nu, ScalarDistanceUtil.getSquaredDistance());
    }

    @Override
    public double distance(double[] A, double[] B) {
        // Create dummy timestamps for A and B.
        double[] tA = createEquidistantTimestamps(A);
        double[] tB = createEquidistantTimestamps(B);

        return calculateDistance(A, tA, B, tB);
    }

    @Override
    public double distance(double[] A, double[] tA, double[] B, double[] tB) {
        return calculateDistance(A, tA, B, tB);
    }

    /**
     * 
     * @param A  Time series A[1..n]
     * @param tA Timestamps of time series A, tA[1..n]
     * @param B  Time Series B[1..m]
     * @param tB Timestamps of time series B, tB[1..m]
     * @return
     */
    private double calculateDistance(double[] A, double[] tA, double[] B, double[] tB) {
        int n = A.length;
        int m = B.length;

        // DP[0..n, 0..m]
        double[][] DP = new double[n + 1][m + 1];

        // declare A[0] := 0, tA[0] := 0
        // declare B[0] := 0, tB[0] := 0
        // Note: Zero pad A and B, i.e. when referencing A[i] use A[i-1], when
        // referencing A[i-1] use A[i-2]

        // Dynamic Programming initialization.
        for (int i = 1; i <= n; i++)
            DP[i][0] = Double.MAX_VALUE;
        for (int i = 1; i <= m; i++)
            DP[0][i] = Double.MAX_VALUE;
        DP[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {

                // Cost for Deletion in A.
                double c1;
                // Cost for Deletion in B.
                double c2;
                // Cost for a match.
                double c3;

                if (i == 1 && j == 1) {
                    // Substitute A[i-2] with 0 and B[j-2] with 0.
                    c1 = DP[i - 1][j] + d.distance(0, A[i - 1]) + nu * tA[i - 1] + lambda;
                    c2 = DP[i][j - 1] + d.distance(0, B[j - 1]) + nu * tB[j - 1] + lambda;
                    c3 = DP[i - 1][j - 1] + d.distance(A[i - 1], B[i - 1]) + nu * Math.abs(tA[i - 1] - tB[j - 1]);
                } else if (i == 1) {
                    // Substitute A[i-2] with 0.
                    c1 = DP[i - 1][j] + d.distance(0, A[i - 1]) + nu * tA[i - 1] + lambda;
                    c2 = DP[i][j - 1] + d.distance(B[j - 2], B[j - 1]) + nu * (tB[j - 1] - tB[j - 2]) + lambda;
                    c3 = DP[i - 1][j - 1] + d.distance(A[i - 1], B[i - 1]) + d.distance(0, B[j - 2])
                            + nu * (Math.abs(tA[i - 1] - tB[j - 1]) + tB[j - 2]);
                } else if (j == 1) {
                    // Substitute B[j-2] with 0.
                    c1 = DP[i - 1][j] + d.distance(A[i - 2], A[i - 1]) + nu * (tA[i - 1] - tA[i - 2]) + lambda;
                    c2 = DP[i][j - 1] + d.distance(0, B[j - 1]) + nu * tB[j - 1] + lambda;
                    c3 = DP[i - 1][j - 1] + d.distance(A[i - 1], B[i - 1]) + d.distance(A[i - 2], 0)
                            + nu * (Math.abs(tA[i - 1] - tB[j - 1]) + tA[i - 2]);
                } else {
                    // No substitution.
                    c1 = DP[i - 1][j] + d.distance(A[i - 2], A[i - 1]) + nu * (tA[i - 1] - tA[i - 2]) + lambda;
                    c2 = DP[i][j - 1] + d.distance(B[j - 2], B[j - 1]) + nu * (tB[j - 1] - tB[j - 2]) + lambda;
                    c3 = DP[i - 1][j - 1] + d.distance(A[i - 1], B[i - 1]) + d.distance(A[i - 2], B[j - 2])
                            + nu * (Math.abs(tA[i - 1] - tB[j - 1]) + Math.abs(tA[i - 2] - tB[j - 2]));
                }

                // Minimum cost.
                double minimum = Math.min(c1, Math.min(c2, c3));
                DP[i][j] = minimum;
            }
        }

        return DP[n][m];
    }

}