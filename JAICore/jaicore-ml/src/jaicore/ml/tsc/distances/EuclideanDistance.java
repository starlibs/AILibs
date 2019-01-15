package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * EuclideanDistance for time series.
 */
public class EuclideanDistance implements ITimeSeriesDistance {

    @Override
    public double distance(INDArray A, INDArray B) throws TimeSeriesLengthException {
        return A.distance2(B);
    }

}