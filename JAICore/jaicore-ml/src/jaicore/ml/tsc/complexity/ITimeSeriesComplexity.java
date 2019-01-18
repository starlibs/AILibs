package jaicore.ml.tsc.complexity;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Interface that describes the complexity measure of a time series.
 */
public interface ITimeSeriesComplexity {

    public double complexity(INDArray T);
}