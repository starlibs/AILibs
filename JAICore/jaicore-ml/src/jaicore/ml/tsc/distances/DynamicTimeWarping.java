package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;

import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * Class for the Time Warp Distance Calculation.
 */
public class DynamicTimeWarping implements IDistance {

    @Override
    public double distance(INDArray timeSeries1, INDArray timeSeries2) throws IllegalArgumentException {
        // Parameter checks.
        isTimeSeriesOrException(timeSeries1);
        isTimeSeriesOrException(timeSeries2);
        isSameLengthOrException(timeSeries1, timeSeries2);

        int n = (int) timeSeries1.length();
        double[][] matrix = new double[n + 1][n + 1];

        // Dynamic Programming initialization.
        for (int i = 1; i <= n; i++)
            matrix[i][0] = Double.MAX_VALUE;
        for (int i = 1; i <= n; i++)
            matrix[0][i] = Double.MAX_VALUE;
        matrix[0][0] = 0d;

        // Dynamic programming.
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                double cost = Math.abs(timeSeries1.getDouble(i - 1) - timeSeries2.getDouble(j - 1));
                double mini = Math.min(matrix[i - 1][j], Math.min(matrix[i][j - 1], matrix[i - 1][j - 1]));
                matrix[i][j] = cost + mini;
            }
        }

        return matrix[n][n];
    }

}