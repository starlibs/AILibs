package jaicore.ml.tsc.complexity;

import org.nd4j.linalg.api.ndarray.INDArray;
import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

/**
 * Stretching Complexity metric as described in "A Complexity-Invariant Distance
 * Measure for Time Series". Note that the equation in the paper is not correct.
 * 
 * $$ c = sum_{i=1}^n-1 \sqrt{ (t_2 - t_1)^2 + (T_{i+1} - T_i)^2 }$$
 * 
 * where $t_i$ are the timestamps (here $t_i = i$) an $T_i$ are the values of
 * the time series.
 */
public class StretchingComplexity implements ITimeSeriesComplexity {

    @Override
    public double complexity(INDArray T) {
        // Parameter checks.
        isTimeSeriesOrException(T);

        int n = (int) T.length();
        double sum = .0;
        for (int i = 0; i < n - 1; i++) {
            sum += Math.sqrt(1 + Math.pow(T.getDouble(i + 1) - T.getDouble(i), 2));
        }

        return sum;
    }

}