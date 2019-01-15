package jaicore.ml.tsc.complexity;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Test cases for the Stretching Complexity measure.
 */
public class StretchingComplexityTest {

    INDArray timeSeries1;
    INDArray timeSeries2;

    INDArray noTimeSeries;

    @Before
    public void setUp() {
        int[] shape = { 6 };
        float[] data = { 1, 1, 1, 1, 1, 1 };
        timeSeries1 = Nd4j.create(data, shape);
        timeSeries2 = Nd4j.create(new double[] { .0, Math.sqrt(8), .0, Math.sqrt(8), .0, Math.sqrt(8) }, shape);

        noTimeSeries = Nd4j.rand(2, 2);
    }

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

    @Test
    public void testThrowsErrorWhenTimeSeriesIsNoTimeSeries() {
        StretchingComplexity sc = new StretchingComplexity();
        assertThrows(IllegalArgumentException.class, () -> {
            sc.complexity(noTimeSeries);
        });
    }
}