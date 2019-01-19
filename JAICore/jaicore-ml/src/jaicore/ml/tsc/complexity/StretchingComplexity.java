package jaicore.ml.tsc.complexity;

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
    public double complexity(double[] T) {
        int n = (int) T.length;
        double sum = .0;
        for (int i = 0; i < n - 1; i++) {
            sum += Math.sqrt(1 + Math.pow(T[i + 1] - T[i], 2));
        }
        return sum;
    }

}