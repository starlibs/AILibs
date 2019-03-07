package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import jaicore.ml.tsc.util.ScalarDistanceUtil;

/**
 * Test suite for the
 * {@link jaicore.ml.tsc.distances.WeightedDynamicTimeWarping} implementation.
 */
public class WeightedDynamicTimeWarpingTest {

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

        double g = 1;
        double maximumWeight = 1;
        IScalarDistance d = ScalarDistanceUtil.getSquaredDistance();
        WeightedDynamicTimeWarping wdtw = new WeightedDynamicTimeWarping(g, maximumWeight, d);
        double distance = wdtw.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 1.0E-5);
    }

}