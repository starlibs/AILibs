package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import jaicore.ml.tsc.util.ScalarDistanceUtil;

/**
 * WeightedDynamicTimeWarpingTest
 */
public class WeightedDynamicTimeWarpingTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries3 = { 0.50, 0.87, 0.90, 0.82, 0.70 };
    double[] timeSeries4 = { 0.10, 0.10, 0.10, 0.10, 0.10 };

    @Test
    public void testDistanceCalculation() {
        int p = 1;
        double g = 1;
        double Wmax = 1;
        IScalarDistance d = ScalarDistanceUtil.getSquaredDistance();
        WeightedDynamicTimeWarping wdtw = new WeightedDynamicTimeWarping(p, g, Wmax, d);
        double distance = wdtw.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        assertEquals(expectation, distance, 1.0E-5);
    }

}