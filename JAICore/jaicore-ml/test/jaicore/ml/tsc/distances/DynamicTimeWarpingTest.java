package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

/**
 * Test suite for the {@link jaicore.ml.tsc.distances.DynamicTimeWarping}
 * implementation.
 */
public class DynamicTimeWarpingTest {

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation() {
        // Input.
        double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
        double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };
        // Expectation.
        double expectation = 0;

        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 1.0E-5);
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation2() {
        // Input.
        double[] timeSeries1 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
        double[] timeSeries2 = { 0.10, 0.10, 0.10, 0.10, 0.10 };
        // Expectation.
        double expectation = 3.29;

        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 1.0E-5);
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation3() {
        // Input.
        double[] timeSeries5 = { 1, 1, 2, 2, 3, 5 }; // distance with d(x,y) = |x-y| is 1
        double[] timeSeries6 = { 1, 2, 3, 5, 5, 6 };
        // Expectation.
        double expectation = 1;

        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distance(timeSeries5, timeSeries6);

        assertEquals(expectation, distance, 1.0E-5);
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testDistanceCalculationWithWindow() {
        // Input.
        double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
        double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 };
        // Expectation.
        double expectation = 3.29;

        DynamicTimeWarping dtw = new DynamicTimeWarping();
        double distance = dtw.distanceWithWindow(timeSeries3, timeSeries4, 10000);

        assertEquals(expectation, distance, 1.0E-5);
    }

    /**
     * Robustness test: When initializing with <code>null</code> for the scalar
     * distance measure, the constructor is supposed to throw an
     * IllegalArgumentExpection.
     */
    @Test
    public void testRobustnessForNullDistanceMeasure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DynamicTimeWarping(null);
        });
    }

}