package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * MoveSplitMerge
 */
public class MoveSplitMerge implements ITimeSeriesDistance {

    private double c;

    /**
     * Constructor.
     * 
     * @param c The constant cost for split and merge operations.
     */
    public MoveSplitMerge(double c) {
        this.c = c;
    }

    @Override
    public double distance(INDArray A, INDArray B) throws TimeSeriesLengthException {
        // Parameter checks.
        isTimeSeriesOrException(A, B);
        isSameLengthOrException(A, B);

        int n = (int) A.length(); // TODO This distance metric also works with non-equal length
        double[][] Cost = new double[n][n];

        // Initialization.
        for (int i = 1; i < n; i++) {
            Cost[i][0] = Cost[i - 1][0] + C(A.getDouble(i), A.getDouble(i - 1), B.getDouble(0));
            Cost[0][i] = Cost[0][i - 1] + C(B.getDouble(i), A.getDouble(0), B.getDouble(i - 1));
        }

        // Dynamic programming.
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                double costMove = Cost[i - 1][j - 1] + Math.abs(A.getDouble(i) - B.getDouble(j));
                double cost2 = Cost[i - 1][j] + C(A.getDouble(i), A.getDouble(i - 1), B.getDouble(j));
                double cost3 = Cost[i][j - 1] + C(B.getDouble(j), A.getDouble(i), B.getDouble(j - 1));
                Cost[i][j] = Math.min(costMove, Math.min(cost2, cost3));
            }
        }

        return Cost[n - 1][n - 1];
    }

    private double C(double x, double xBefore, double y) {
        if ((xBefore <= x && x <= y) || (xBefore >= x && x >= y)) {
            // x_{i-1} <= x_i <= y_j or x_{i-1} >= x_i >= y_j
            return c;
        } else {
            return c + Math.min(Math.abs(x - xBefore), Math.abs(x - y));
        }
    }

}