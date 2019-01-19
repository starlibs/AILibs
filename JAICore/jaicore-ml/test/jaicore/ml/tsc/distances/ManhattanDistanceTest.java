package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Test;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * ManhattanDistanceTest
 */
public class ManhattanDistanceTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 }; // { 1, 1, 1, 1, 1, 1 }
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 }; // { 1, 1, 1, 1, 1, 1 }
    double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 }; // { 0.50, 0.87, 0.90, 0.82, 0.70 }
    double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 }; // { 0.10, 0.10, 0.10, 0.10, 0.10 }

    @Test
    public void testDistanceCalculation() throws TimeSeriesLengthException {
        EuclideanDistance md = new EuclideanDistance();
        double distance = md.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        assertEquals(expectation, distance, 1.0E-5);
    }

    @Test
    public void testDistanceCalculation2() throws TimeSeriesLengthException {
        ManhattanDistance md = new ManhattanDistance();
        double distance = md.distance(timeSeries3, timeSeries4);
        double expectation = 3.29;
        assertEquals(expectation, distance, 1.0E-5);
    }

    @Test
    public void testThrowsErrorWhenTimeSeriesHavmdifferentLength() {
        ManhattanDistance dtw = new ManhattanDistance();
        assertThrows(TimeSeriesLengthException.class, () -> {
            dtw.distance(timeSeries1, timeSeries4);
        });
    }
}