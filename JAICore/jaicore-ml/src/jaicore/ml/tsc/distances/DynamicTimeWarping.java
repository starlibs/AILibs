package jaicore.ml.tsc.distances;

import static jaicore.ml.tsc.util.TimeSeriesUtil.*;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * Class for the Time Warp Distance Calculation.
 */
public class DynamicTimeWarping implements ITimeSeriesDistance {

    /** Distance measure for scalar points. */
    IScalarDistance d;

    /**
     * Creates an instance with absolute distance as point distance.
     */
    public DynamicTimeWarping() {
        this((x, y) -> Math.abs(x - y));
    }

    /**
     * Creates an instance with a given scalar distance measure.
     * 
     * @param d Scalar distance measure.
     */
    public DynamicTimeWarping(IScalarDistance d) {
        this.d = d;
    }

    @Override
    public double distance(double[] A, double[] B) throws TimeSeriesLengthException {
        // Parameter checks.
        isSameLengthOrException(A, B);

        int n = A.length;
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
                double cost = d.distance(A[i - 1], B[j - 1]);
                double mini = Math.min(M[i - 1][j], Math.min(M[i][j - 1], M[i - 1][j - 1]));
                M[i][j] = cost + mini;
            }
        }

        return M[n][n];
    }

}