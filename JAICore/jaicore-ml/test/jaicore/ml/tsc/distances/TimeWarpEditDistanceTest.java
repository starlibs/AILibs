package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import jaicore.ml.tsc.util.ScalarDistanceUtil;

/**
 * Test suite for the {@link jaicore.ml.tsc.distances.TimeWarpEditDistance}
 * implementation.
 */
public class TimeWarpEditDistanceTest {

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

        double lambda = 1.0;
        double nu = 0.001;
        TimeWarpEditDistance twed = new TimeWarpEditDistance(lambda, nu);
        double distance = twed.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 0);
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testCorrectnessForDistanceCalculation2() {
        // Input.
        double[] timeSeries1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        double[] timeSeries2 = { 1, 2, 3, 4, 5, 6, 7, 8, 12 };
        // Expectation.
        double expectation = 3;

        double lambda = 1.0;
        double nu = 0.001;
        IScalarDistance d = ScalarDistanceUtil.getAbsoluteDistance();
        TimeWarpEditDistance twed = new TimeWarpEditDistance(lambda, nu, d);
        double distance = twed.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 0);
    }

    /**
     * Correctness test. Tests the distance calculation based on an defined input
     * and expected output.
     */
    @Test
    public void testDistanceCalculation3() {
        // Input.
        double[] timeSeries1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        double[] timeSeries2 = { 0, 3, 2, 5, 4, 7, 6, 9, 8 };
        // Expectation.
        double expectation = 17;

        double lambda = 1.0;
        double nu = 0.001;
        IScalarDistance d = ScalarDistanceUtil.getAbsoluteDistance();
        TimeWarpEditDistance twed = new TimeWarpEditDistance(lambda, nu, d);
        double distance = twed.distance(timeSeries1, timeSeries2);

        assertEquals(expectation, distance, 0);
    }

    /**
     * Robustness test: When initializing with <code>lambda < 0</code> the
     * constuctor is supposed to thrown an IllegalArgumentException.
     */
    @Test
    public void testRobustnessForLambdaLessThanZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            double lambda = 0 - Double.MIN_VALUE;
            new TimeWarpEditDistance(lambda, 1, ScalarDistanceUtil.getAbsoluteDistance());
        });
    }

    /**
     * Robustness test: When initializing with <code>nu < 0</code> the constuctor is
     * supposed to thrown an IllegalArgumentException.
     */
    @Test
    public void testRobustnessForNuLessThanZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            double nu = 0 - Double.MIN_VALUE;
            new TimeWarpEditDistance(1, nu, ScalarDistanceUtil.getAbsoluteDistance());
        });
    }

    /**
     * Boundary test: When initializing with <code>alpha = pi/2</code> the
     * constructor is must not thrown an IllegalArgumentException.
     */
    @Test
    public void testBoundaryForLambdaEqualToZero() {
        double lambda = 0;
        new TimeWarpEditDistance(lambda, 1, ScalarDistanceUtil.getAbsoluteDistance());
    }

    /**
     * Boundary test: When initializing with <code>nu = 0</code> the constructor is
     * must not thrown an IllegalArgumentException.
     */
    @Test
    public void testBoundaryForNuEqualToZero() {
        double nu = 0;
        new TimeWarpEditDistance(1, nu, ScalarDistanceUtil.getAbsoluteDistance());
    }

}