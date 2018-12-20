package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;

import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * Class for the Time Warp Distance Calculation.
 */
public class DynamicTimeWarping implements IDistance {

    @Override
    public double distance(INDArray A, INDArray tA, INDArray B, INDArray tB) throws IllegalArgumentException {
        // Parameter checks.
        isTimeSeriesOrException(A, tA, B, tB);
        isSameLengthOrException(A, tA, B, tB);

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
                double cost = Math.abs(A.getDouble(i - 1) - B.getDouble(j - 1));
                double mini = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + mini;
            }
        }

        return M[n][n];
    }

}