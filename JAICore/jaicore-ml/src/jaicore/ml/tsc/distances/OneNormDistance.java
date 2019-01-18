package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * OneNormDistance (L1-Norm) for time series.
 */
public class OneNormDistance implements ITimeSeriesDistance {

    @Override
    public double distance(INDArray A, INDArray B) throws TimeSeriesLengthException {
        return A.distance1(B);
    }

}