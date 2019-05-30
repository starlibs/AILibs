package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * Implementation of the Euclidean distance for time series.
 * 
 * The Euclidean distance for two time series <code>A</code> and <code>B</code>
 * of length <code>n</code> is defined as
 * <code>\sqrt{\sum_{i=0}^{n} (A_i - B_i)^2 }</code>. Therefore, it is required
 * for <code>A</code> and <code>B</code> to be of the same length.
 * 
 * @author fischor
 */
public class EuclideanDistance implements ITimeSeriesDistance {

    @Override
    public double distance(double[] A, double[] B) throws TimeSeriesLengthException {
        // Parameter checks.
        isSameLengthOrException(A, B);

        int n = A.length;
        // Sum over all elements.
        double result = 0;
        for (int i = 0; i < n; i++) {
            result += Math.pow((A[i] - B[i]), 2);
        }
        result = Math.sqrt(result);

        return result;
    }

}