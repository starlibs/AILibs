package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Test;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * TimeWarpEditDistanceTest
 */
public class TimeWarpEditDistanceTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
    double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 };

    @Test
    public void testDistanceCalculation() throws TimeSeriesLengthException {
        TimeWarpEditDistance twed = new TimeWarpEditDistance(1.0, 1.0);
        double distance = twed.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    @Test
    public void testThrowsErrorWhenTimeSeriesHaveDifferentLength() {
        TimeWarpEditDistance twed = new TimeWarpEditDistance(1.0, 1.0);
        assertThrows(TimeSeriesLengthException.class, () -> {
            twed.distance(timeSeries1, timeSeries4);
        });
    }
}