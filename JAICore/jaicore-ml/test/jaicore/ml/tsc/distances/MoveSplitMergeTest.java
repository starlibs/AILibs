package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test suite for the {@link jaicore.ml.tsc.distances.MoveSplitMerge}
 * implementation.
 */
public class MoveSplitMergeTest {

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

        MoveSplitMerge msm = new MoveSplitMerge(1.0);
        double distance = msm.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 0);
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation2() {
        // Input.
        double[] timeSeries1 = { 7.0, 8.0, 12.0, 15.0, 9.0, 3.0, 5.0 };
        double[] timeSeries2 = { 7.0, 9.0, 12.0, 15.0, 15.0, 9.0, 3.0 };
        // Expectation.
        double expectation = 11;

        double c = 4.0;
        MoveSplitMerge msm = new MoveSplitMerge(c);
        double distance = msm.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 0);
    }

}