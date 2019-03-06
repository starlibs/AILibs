package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * DynamicTimeWarpingTest
 */
public class DynamicTimeWarpingTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };

    double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 }; // distance with
    double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 };

    double[] timeSeries5 = { 1, 1, 2, 2, 3, 5 }; // distance with d(x,y) = |x-y| is 1
    double[] timeSeries6 = { 1, 2, 3, 5, 5, 6 };

    @Test
    public void testDistanceCalculation() {
        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        assertEquals(expectation, distance, 1.0E-5);
    }

    @Test
    public void testDistanceCalculation2() {
        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries3, timeSeries4);
        double expectation = 3.29;
        assertEquals(expectation, distance, 1.0E-5);
    }

    @Test
    public void testDistanceCalculationWithWindow() {
        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distanceWithWindow(timeSeries3, timeSeries4, 10000);
        double expectation = 3.29;
        assertEquals(expectation, distance, 1.0E-5);
    }

    @Test
    public void testDistanceCalculation3() {
        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries5, timeSeries6);
        double expectation = 1;
        assertEquals(expectation, distance, 1.0E-5);
    }

}