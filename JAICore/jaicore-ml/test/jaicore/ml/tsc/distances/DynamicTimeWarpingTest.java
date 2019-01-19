package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Test;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * DynamicTimeWarpingTest
 */
public class DynamicTimeWarpingTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
    double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 };

    @Test
    public void testDistanceCalculation() throws TimeSeriesLengthException {
        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        assertEquals(expectation, distance, 1.0E-5);
    }

    @Test
    public void testDistanceCalculation2() throws TimeSeriesLengthException {
        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries3, timeSeries4);
        double expectation = 3.29;
        assertEquals(expectation, distance, 1.0E-5);
    }

    @Test
    public void testThrowsErrorWhenTimeSeriesHaveDifferentLength() {
        DynamicTimeWarping dtw = new DynamicTimeWarping();
        assertThrows(IllegalArgumentException.class, () -> {
            dtw.distance(timeSeries1, timeSeries4);
        });
    }
}