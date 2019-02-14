package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import jaicore.ml.tsc.util.ScalarDistanceUtil;

/**
 * DerivateTransformDistanceTest
 */
public class DerivateTransformDistanceTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };

    double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 }; // distance with
    double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 };

    double[] timeSeries5 = { 1, 1, 2, 2, 3, 5 }; // distance with d(x,y) = |x-y| is 1
    double[] timeSeries6 = { 1, 2, 3, 5, 5, 6 }; // backward distance derivates have distance 3

    @Test
    public void testDistanceCalculation() throws TimeSeriesLengthException {
        DerivateTransformDistance ddt = new DerivateTransformDistance(0.5);
        double distance = ddt.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        assertEquals(expectation, distance, 0);
    }

    @Test
    public void testDistanceCalculation2() throws TimeSeriesLengthException {
        double alpha = 0.5;
        ITimeSeriesDistance timeSeriesDistance = new DynamicTimeWarping(ScalarDistanceUtil.getAbsoluteDistance());
        DerivateTransformDistance dtw = new DerivateTransformDistance(alpha, timeSeriesDistance);
        double distance = dtw.distance(timeSeries5, timeSeries6);
        double expectation = Math.cos(alpha) * 1 + Math.sin(alpha) * 3;
        assertEquals(expectation, distance, 1.0E-5);
    }

}