package jaicore.ml.tsc.complexity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for the Stretching Complexity measure.
 */
public class SquaredBackwardDifferenceComplexityTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { .0, Math.sqrt(8), .0, Math.sqrt(8), .0, Math.sqrt(8) };

    @Test
    public void testComplexityCalculation() throws IllegalArgumentException {
        SquaredBackwardDifferenceComplexity sc = new SquaredBackwardDifferenceComplexity();
        double complexity = sc.complexity(timeSeries1);
        double expectation = 0;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, complexity, expectation), expectation, complexity, 0.001);
    }

    @Test
    public void testComplexityCalculation2() throws IllegalArgumentException {
        SquaredBackwardDifferenceComplexity sc = new SquaredBackwardDifferenceComplexity();
        double complexity = sc.complexity(timeSeries2);
        double expectation = Math.sqrt(40);
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, complexity, expectation), expectation, complexity, 0.001);
    }
}