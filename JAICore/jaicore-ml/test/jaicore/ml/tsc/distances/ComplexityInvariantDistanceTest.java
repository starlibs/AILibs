package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.tsc.complexity.StretchingComplexity;

/**
 * ComplexityInvariantDistanceTest
 */
public class ComplexityInvariantDistanceTest {

    /** The distance to test. */
    ComplexityInvariantDistance cid;

    /** Time series to test with. */
    INDArray timeSeries1; // complexity 5
    INDArray timeSeries2; // complexity 15
    INDArray noTimeSeries;

    @Before
    public void setUp() {
        int[] shape = { 6 };
        float[] data = { 1, 1, 1, 1, 1, 1 };
        timeSeries1 = Nd4j.create(data, shape);
        timeSeries2 = Nd4j.create(new double[] { .0, Math.sqrt(8), .0, Math.sqrt(8), .0, Math.sqrt(8) }, shape);

        noTimeSeries = Nd4j.rand(2, 2);

        EuclideanDistance e = new EuclideanDistance();
        StretchingComplexity sc = new StretchingComplexity();
        cid = new ComplexityInvariantDistance(e, sc);
    }

    @Test
    public void testDistanceCalculation() throws IllegalArgumentException {
        double distance = cid.distance(timeSeries1, timeSeries2);
        double expectation = timeSeries1.distance2(timeSeries2) * (15 / 5);
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0.001);
    }

    @Test
    public void testThrowsErrorWhenTimeSeriesIsNoTimeSeries() {
        assertThrows(IllegalArgumentException.class, () -> {
            cid.distance(noTimeSeries, timeSeries1);
        });
    }
}