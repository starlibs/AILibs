package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
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
     * Creates a TimeWarpEditDistance with euclidean distance as point distance.
     * 
     * @param lambda Additional cost parameter for deletion.
     * @param nu     Stiffness parameter.
     */
    public TimeWarpEditDistance(double lambda, double nu) {
        this(lambda, nu, ScalarDistanceUtil.getEuclideanDistance());
    }

    @Override
    public double distance(double[] A, double[] B) throws TimeSeriesLengthException {
        // Parameter checks.
        isSameLengthOrException(A, B);
        // Create dummy timestamps for A and B.
        double[] tA = createEquidistantTimestamps(A);
        double[] tB = createEquidistantTimestamps(B);

        return calculateDistance(A, tA, B, tB);
    }

    @Override
    public double distance(double[] A, double[] tA, double[] B, double[] tB) throws TimeSeriesLengthException {
        // Parameter checks.
        isSameLengthOrException(A, tA, B, tB);

        return calculateDistance(A, tA, B, tB);
    }

    private double calculateDistance(double[] A, double[] tA, double[] B, double[] tB) {
        int n = A.length;
        double[][] M = new double[n][n];

        // Dynamic Programming initialization.
        for (int i = 1; i < n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int i = 1; i < n; i++)
            M[0][i] = Double.MAX_VALUE;
        M[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                // Case: Delete in A
                double c1 = M[i - 1][j] + d.distance(A[i], A[i - 1]) + nu * (tA[i] - tA[i - 1]) + lambda;
                // Case: Agreement
                double c2 = M[i - 1][j - 1] + d.distance(A[i], B[j]) + d.distance(A[i - 1], B[j - 1])
                        + nu * (Math.abs(tA[i] - tB[j]) + Math.abs(tA[i - 1] - tB[j - 1]));
                // Case: Delete in B
                double c3 = M[i][j - 1] + d.distance(B[i], B[i - 1]) + nu * (tB[j] - tB[j - 1]) + lambda;
                // Minimum cost.
                double minimum = Math.min(c1, Math.min(c2, c3));
                M[i][j] = minimum;
            }
        }

        return M[n - 1][n - 1];
    }

}