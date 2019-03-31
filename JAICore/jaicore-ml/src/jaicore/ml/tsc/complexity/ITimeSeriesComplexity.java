package jaicore.ml.tsc.complexity;

/**
 * Interface that describes the complexity measure of a time series.
 * 
 * @author fischor
 */
public interface ITimeSeriesComplexity {

    public double complexity(double[] T);
}