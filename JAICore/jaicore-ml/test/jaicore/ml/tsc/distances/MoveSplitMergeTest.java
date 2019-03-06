package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

/**
 * MoveSplitMergeTest
 */
public class MoveSplitMergeTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries3 = { 7.0, 8.0, 12.0, 15.0, 9.0, 3.0, 5.0 };
    double[] timeSeries4 = { 7.0, 9.0, 12.0, 15.0, 15.0, 9.0, 3.0 };
    double[] timeSeries5 = { 7.0, 8.0, 12.0, 15.0, 9.0, 9.0, 5.0 };
    double[] timeSeries6 = { 7.0, 8.0, 12.0, 13.0, 9.0, 9.0, 5.0 };

    @Test
    public void testDistanceCalculation() {
        MoveSplitMerge msm = new MoveSplitMerge(1.0);
        double distance = msm.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    @Test
    public void testDistanceCalculation2() {
        double c = 4.0;
        MoveSplitMerge msm = new MoveSplitMerge(c);
        double distance = msm.distance(timeSeries3, timeSeries4);
        double expectation = 11;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    @Test
    public void testThrowsErrorWhenTimeSeriesHaveDifferentLength() {
        MoveSplitMerge msm = new MoveSplitMerge(1.0);
        assertThrows(IllegalArgumentException.class, () -> {
            msm.distance(timeSeries1, timeSeries4);
        });
    }

}