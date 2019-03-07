package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Test;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * Test suite for the {@link jaicore.ml.tsc.distances.ManhattanDistance}
 * implementation.
 */
public class ManhattanDistanceTest {

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation() throws TimeSeriesLengthException {
        // Input.
        double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
        double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };
        // Expectation.
        double expectation = 0;

        EuclideanDistance md = new EuclideanDistance();
        double distance = md.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 1.0E-5);
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation2() throws TimeSeriesLengthException {
        // Input.
        double[] timeSeries1 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
        double[] timeSeries2 = { 0.10, 0.10, 0.10, 0.10, 0.10 };
        // Expectation.
        double expectation = 3.29;

        ManhattanDistance md = new ManhattanDistance();
        double distance = md.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 1.0E-5);
    }

    /**
     * Robustness test: When trying to calculate between to time series that differ
     * in length, the distance method is supposed to throw an
     * TimeSeriesLengthException.
     */
    @Test
    public void testRobustnessForTimeSeriesWithDifferentLength() {
        double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
        double[] timeSeries2 = { 1, 1, 1, 1, 1, 1, 1 };
        ManhattanDistance dtw = new ManhattanDistance();
        assertThrows(TimeSeriesLengthException.class, () -> {
            dtw.distance(timeSeries1, timeSeries2);
        });
    }
}