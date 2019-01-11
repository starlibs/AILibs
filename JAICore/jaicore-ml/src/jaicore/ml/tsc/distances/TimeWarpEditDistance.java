package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;
import static jaicore.ml.tsc.util.TimeSeriesUtil.*;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * TimeWarpEditDistance
 */
public class TimeWarpEditDistance implements ITimeSeriesDistanceWithTimestamps {

    /** Stiffness parameter. */
    private double nu;

    /** Additional cost parameter for deletion. */
    private double lambda;

    /**
     * Distance mesaure used for point distance calculation. In {"euclidean",
     * "absolute"}.
     */
    private String pointDistance;

    /**
     * Constructor.
     * 
     * @param lambda        Additional cost parameter for deletion.
     * @param nu            Stiffness parameter.
     * @param pointDistance Distance mesaure used for point distance calculation. In
     *                      {"euclidean", "absolute"}.
     */
    public TimeWarpEditDistance(double lambda, double nu, String pointDistance) {
        this.lambda = lambda;
        this.nu = nu;
        this.pointDistance = pointDistance;
    }

    /**
     * Creates a TimeWarpEditDistance with euclidean distance as point distance.
     * 
     * @param lambda Additional cost parameter for deletion.
     * @param nu     Stiffness parameter.
     */
    public TimeWarpEditDistance(double lambda, double nu) {
        this(lambda, nu, "euclidean");
    }

    @Override
    public double distance(INDArray A, INDArray B) throws TimeSeriesLengthException {
        // Parameter checks.
        isTimeSeriesOrException(A, B);
        isSameLengthOrException(A, B);
        // Create dummy timestamps for A and B.
        INDArray tA = TimeSeriesUtil.createEquidistantTimestamps(A);
        INDArray tB = TimeSeriesUtil.createEquidistantTimestamps(B);

        return calculateDistance(A, tA, B, tB);
    }

    @Override
    public double distance(INDArray A, INDArray tA, INDArray B, INDArray tB) throws TimeSeriesLengthException {
        // Parameter checks.
        isTimeSeriesOrException(A, tA, B, tB);
        isSameLengthOrException(A, tA, B, tB);

        return calculateDistance(A, tA, B, tB);
    }

    private double calculateDistance(INDArray A, INDArray tA, INDArray B, INDArray tB) {
        int n = (int) A.length();
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
                double c1 = M[i - 1][j] + d(A.getDouble(i), A.getDouble(i - 1))
                        + nu * (tA.getDouble(i) - tA.getDouble(i - 1)) + lambda;
                // Case: Agreement
                double c2 = M[i - 1][j - 1] + d(A.getDouble(i), B.getDouble(j))
                        + d(A.getDouble(i - 1), B.getDouble(j - 1)) + nu * (Math.abs(tA.getDouble(i) - tB.getDouble(j))
                                + Math.abs(tA.getDouble(i - 1) - tB.getDouble(j - 1)));
                // Case: Delete in B
                double c3 = M[i][j - 1] + d(B.getDouble(i), B.getDouble(i - 1))
                        + nu * (tB.getDouble(j) - tB.getDouble(j - 1)) + lambda;
                // Minimum cost.
                double minimum = Math.min(c1, Math.min(c2, c3));
                M[i][j] = minimum;
            }
        }

        return M[n - 1][n - 1];
    }

    private double d(double a, double b) {
        return Math.sqrt(a * a + b * b);
    }

}