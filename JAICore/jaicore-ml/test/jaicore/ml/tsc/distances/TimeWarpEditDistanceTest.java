package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * TimeWarpEditDistanceTest
 */
public class TimeWarpEditDistanceTest {

    INDArray timeSeries1; // { 1, 1, 1, 1, 1, 1 }
    INDArray timeSeries2; // { 1, 1, 1, 1, 1, 1 }
    INDArray timeSeries3; // { 0.50, 0.87, 0.90, 0.82, 0.70 }
    INDArray timeSeries4; // { 0.10, 0.10, 0.10, 0.10, 0.10 }

    INDArray noTimeSeries;

    @Before
    public void setUp() {
        int[] shape = { 6 };
        float[] data = { 1, 1, 1, 1, 1, 1 };
        timeSeries1 = Nd4j.create(data, shape);
        timeSeries2 = Nd4j.create(data, shape);

        int[] shape2 = { 5 };
        timeSeries3 = Nd4j.create(new double[] { 0.50, 0.87, 0.90, 0.82, 0.70 }, shape2);
        timeSeries4 = Nd4j.create(new double[] { 0.10, 0.10, 0.10, 0.10, 0.10 }, shape2);

        noTimeSeries = Nd4j.rand(2, 2);
    }

    @Test
    public void testDistanceCalculation() throws IllegalArgumentException {
        TimeWarpEditDistance twed = new TimeWarpEditDistance(1.0, 1.0);
        double distance = twed.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }
}