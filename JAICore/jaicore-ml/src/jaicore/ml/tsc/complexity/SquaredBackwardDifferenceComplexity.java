package jaicore.ml.tsc.complexity;

/**
 * Complexity metric as described in "A Complexity-Invariant Distance Measure
 * for Time Series".
 * 
 * $$ c = sum_{i=1}^n-1 \sqrt{ (T_I - T_{i+1})^2 }$$
 * 
 * where $T_i$ are the values of the time series.
 */
public class SquaredBackwardDifferenceComplexity implements ITimeSeriesComplexity {

    @Override
    public double complexity(double[] T) {
        int n = (int) T.length;
        double sum = .0;
        for (int i = 0; i < n - 1; i++) {
            sum += (T[i] - T[i + 1]) * (T[i] - T[i + 1]);
        }
        return Math.sqrt(sum);
    }

}