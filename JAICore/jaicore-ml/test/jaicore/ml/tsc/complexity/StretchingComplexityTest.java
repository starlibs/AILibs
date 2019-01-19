package jaicore.ml.tsc.complexity;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test cases for the Stretching Complexity measure.
 */
public class StretchingComplexityTest {

    double[] timeSeries1 = { 1, 1, 1, 1, 1, 1 };
    double[] timeSeries2 = { .0, Math.sqrt(8), .0, Math.sqrt(8), .0, Math.sqrt(8) };

    @Test
    public void testComplexityCalculation() throws IllegalArgumentException {
        StretchingComplexity sc = new StretchingComplexity();
        double complexity = sc.complexity(timeSeries1);
        double expectation = 5;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, complexity, expectation), expectation, complexity, 0.001);
    }

    @Test
    public void testComplexityCalculation2() throws IllegalArgumentException {
        StretchingComplexity sc = new StretchingComplexity();
        double complexity = sc.complexity(timeSeries2);
        double expectation = 15;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, complexity, expectation), expectation, complexity, 0.001);
    }
}