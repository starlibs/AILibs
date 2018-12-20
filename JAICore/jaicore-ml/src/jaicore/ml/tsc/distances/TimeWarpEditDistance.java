package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;
import static jaicore.ml.tsc.util.TimeSeriesUtil.*;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * TimeWarpEditDistance
 */
public class TimeWarpEditDistance implements IDistance {

    double nu;
    double lambda;
    String pointDistance;

    public TimeWarpEditDistance(double lambda, double nu, String pointDistance) {
        this.lambda = lambda;
        this.nu = nu;
        this.pointDistance = pointDistance;
    }

    /**
     * 
     * @param lambda
     * @param nu     Stiffness parameter.
     */
    public TimeWarpEditDistance(double lambda, double nu) {
        this(lambda, nu, "euclidean");
    }

    @Override
    public double distance(INDArray A, INDArray tA, INDArray B, INDArray tB) throws TimeSeriesLengthException {
        // Parameter checks.
        isTimeSeriesOrException(A);
        isTimeSeriesOrException(B);
        isSameLengthOrException(A, B);

        int n = (int) A.length();
        double[][] M = new double[n + 1][n + 1];

        // Dynamic Programming initialization.
        for (int i = 1; i <= n; i++)
            M[i][0] = Double.MAX_VALUE;
        for (int i = 1; i <= n; i++)
            M[0][i] = Double.MAX_VALUE;
        M[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
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

                double minimum = Math.min(c1, Math.min(c2, c3));
                M[i][j] = minimum;
            }
        }

        return M[n][n];
    }

    private double d(double a, double b) {
        return Math.sqrt(a * a + b * b);
    }

}