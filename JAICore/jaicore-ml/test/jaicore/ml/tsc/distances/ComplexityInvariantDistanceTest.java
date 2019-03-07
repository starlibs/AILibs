package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;

import jaicore.ml.tsc.complexity.StretchingComplexity;

/**
 * Test suite for the
 * {@link jaicore.ml.tsc.distances.ComplexityInvariantDistance} implementation.
 */
public class ComplexityInvariantDistanceTest {

    /** The distance measure used throughout the tests. */
    EuclideanDistance euclideanDistance;

    /** The complexity measure used throughout the tests. */
    StretchingComplexity stretchingComplexity;

    @Before
    public void setUp() {
        euclideanDistance = new EuclideanDistance();
        stretchingComplexity = new StretchingComplexity();
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation() {
        // Input.
        double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 }; // complexity 5
        double[] timeSeries2 = { .0, Math.sqrt(8), .0, Math.sqrt(8), .0, Math.sqrt(8) }; // complexity 15
        // Expectation.
        double expectation = euclideanDistance.distance(timeSeries1, timeSeries2) * (15 / 5);

        ComplexityInvariantDistance cid = new ComplexityInvariantDistance(euclideanDistance, stretchingComplexity);
        double distance = cid.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 0.001);
    }

    /**
     * Robustness test: When initializing with <code>null</code> for the distance
     * measure, the constructor is supposed to throw an IllegalArgumentExpection.
     */
    @Test
    public void testRobustnessForNullDistanceMeasure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ComplexityInvariantDistance(null, stretchingComplexity);
        });
    }

    /**
     * Robustness test: When initializing with <code>null</code> for the complexity
     * measure, the constructor is supposed to throw an IllegalArgumentExpection.
     */
    @Test
    public void testRobustnessForNullComplexityMeasure() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ComplexityInvariantDistance(euclideanDistance, null);
        });
    }

}