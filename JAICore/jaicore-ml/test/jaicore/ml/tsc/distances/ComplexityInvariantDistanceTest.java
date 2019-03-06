package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import jaicore.ml.tsc.complexity.StretchingComplexity;

/**
 * ComplexityInvariantDistanceTest
 */
public class ComplexityInvariantDistanceTest {

    /** The distance to test. */
    ComplexityInvariantDistance cid;

    EuclideanDistance ed;

    /** Time series to test with. */
    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 }; // complexity 5
    double[] timeSeries2 = { .0, Math.sqrt(8), .0, Math.sqrt(8), .0, Math.sqrt(8) }; // complexity 15
    double[] noTimeSeries;

    @Before
    public void setUp() {
        ed = new EuclideanDistance();
        StretchingComplexity sc = new StretchingComplexity();
        cid = new ComplexityInvariantDistance(ed, sc);
    }

    @Test
    public void testDistanceCalculation() {
        double distance = cid.distance(timeSeries1, timeSeries2);
        double expectation = ed.distance(timeSeries1, timeSeries2) * (15 / 5);
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0.001);
    }
}