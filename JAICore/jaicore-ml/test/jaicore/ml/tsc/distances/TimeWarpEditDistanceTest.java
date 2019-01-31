package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import jaicore.ml.tsc.util.ScalarDistanceUtil;

/**
 * TimeWarpEditDistanceTest
 */
public class TimeWarpEditDistanceTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };

    double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
    double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 };

    double[] timeSeries5 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    double[] timeSeries6 = { 1, 2, 3, 4, 5, 6, 7, 8, 12 };
    double[] timeSeries7 = { 0, 3, 2, 5, 4, 7, 6, 9, 8 };

    @Test
    public void testDistanceCalculation() throws TimeSeriesLengthException {
        TimeWarpEditDistance twed = new TimeWarpEditDistance(1.0, 1.0);
        double distance = twed.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    @Test
    public void testDistanceCalculation2() throws TimeSeriesLengthException {
        TimeWarpEditDistance twed = new TimeWarpEditDistance(1.0, 0.001, ScalarDistanceUtil.getAbsoluteDistance());
        double distance = twed.distance(timeSeries5, timeSeries6);
        // http://dekalogblog.blogspot.com/2017/12/time-warp-edit-distance.html
        double expectation = 3;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    @Test
    public void testDistanceCalculation3() throws TimeSeriesLengthException {
        TimeWarpEditDistance twed = new TimeWarpEditDistance(1.0, 0.001, ScalarDistanceUtil.getAbsoluteDistance());
        double distance = twed.distance(timeSeries5, timeSeries7);
        // http://dekalogblog.blogspot.com/2017/12/time-warp-edit-distance.html
        double expectation = 17;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

}