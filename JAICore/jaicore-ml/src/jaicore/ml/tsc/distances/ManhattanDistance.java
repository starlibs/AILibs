package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * Manhattan distance for time series (vectors).
 */
public class ManhattanDistance implements ITimeSeriesDistance {

    @Override
    public double distance(double[] A, double[] B) throws TimeSeriesLengthException {
        // Parameter checks.
        isSameLengthOrException(A, B);

        int n = A.length;
        double result = 0;
        for (int i = 0; i < n; i++) {
            result += Math.abs(A[i] - B[i]);
        }
        return result;
    }

}