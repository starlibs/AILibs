package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * Implementation of the Manhattan distance for time series.
 * 
 * The Euclidean distance for two time series <code>A</code> and <code>B</code>
 * of length <code>n</code> is defined as
 * <code>\sum_{i=0}^{n} |A_i - B_i|</code>. Therefore, it is required for
 * <code>A</code> and <code>B</code> to be of the same length.
 */
public class ManhattanDistance implements ITimeSeriesDistance {

    @Override
    public double distance(double[] A, double[] B) throws TimeSeriesLengthException {
        // Parameter checks.
        isSameLengthOrException(A, B);

        int n = A.length;
        // Sum over all elements.
        double result = 0;
        for (int i = 0; i < n; i++) {
            result += Math.abs(A[i] - B[i]);
        }

        return result;
    }

}