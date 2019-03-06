package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import jaicore.ml.tsc.util.ScalarDistanceUtil;

/**
 * TimeWarpEditDistanceTest
 */
public class TimeWarpEditDistanceTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    double[] timeSeries3 = { 1, 2, 3, 4, 5, 6, 7, 8, 12 };
    double[] timeSeries4 = { 0, 3, 2, 5, 4, 7, 6, 9, 8 };

    /**
     * Test the distance calculation on equal time series. The expected result is
     * <code>0</code>.
     */
    @Test
    public void testDistanceCalculation() {
        TimeWarpEditDistance twed = new TimeWarpEditDistance(1.0, 1.0);
        double distance = twed.distance(timeSeries1, timeSeries1);
        double expectation = 0;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    /**
     * Test the distance calculation with <code>lambda = 1</code> and
     * <code>nu=0.001</code> for the timeseries <code> 
     * s = { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
     * t = { 1, 2, 3, 4, 5, 6, 7, 8, 12 }
     * </code>. The expected result, following this <a href="//
     * http://dekalogblog.blogspot.com/2017/12/time-warp-edit-distance.html">reference</a>
     * is <code>3</code>.
     */
    @Test
    public void testDistanceCalculation2() {
        double lambda = 1.0;
        double nu = 0.001;
        TimeWarpEditDistance twed = new TimeWarpEditDistance(lambda, nu, ScalarDistanceUtil.getAbsoluteDistance());
        double distance = twed.distance(timeSeries2, timeSeries3);

        double expectation = 3;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    /**
     * Test the distance calculation with <code>lambda = 1</code> and
     * <code>nu=0.001</code> for the timeseries <code> 
     * s = { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
     * t = { 0, 3, 2, 5, 4, 7, 6, 9, 8 }
     * </code>. The expected result, following this <a href="//
     * http://dekalogblog.blogspot.com/2017/12/time-warp-edit-distance.html">reference</a>
     * is <code>17</code>.
     */
    @Test
    public void testDistanceCalculation3() {
        double lambda = 1.0;
        double nu = 0.001;
        TimeWarpEditDistance twed = new TimeWarpEditDistance(lambda, nu, ScalarDistanceUtil.getAbsoluteDistance());
        double distance = twed.distance(timeSeries2, timeSeries4);
        double expectation = 17;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

}