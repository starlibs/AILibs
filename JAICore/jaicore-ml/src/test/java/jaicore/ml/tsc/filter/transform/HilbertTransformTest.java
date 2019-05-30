package jaicore.ml.tsc.filter.transform;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Test suite for the {@link HilbertTransform} implementation.
 * 
 * @author fischor
 */
public class HilbertTransformTest {

    @Test
    public void testCorrectnessForTransformation() {
        // Input.
        double[] timeSeries = { 1, 2, 3, 4, 5 };
        // Expectation.
        double[] expectation = { -6.0833, -5.6666667, -4, -0.6666667, 6.41666667 };

        HilbertTransform hilbertTransform = new HilbertTransform();
        double[] transformed = hilbertTransform.transform(timeSeries);

        assertArrayEquals(expectation, transformed, 0.0001);
    }

    @Test
    public void testCorrectnessForTransformation2() {
        // Input.
        double[] timeSeries = { 2, 2, 2, 2, 2 };
        // Expectation.
        double[] expectation = { -4.166667, -1.666667, 0, 1.666667, 4.166667 };

        HilbertTransform hilbertTransform = new HilbertTransform();
        double[] transformed = hilbertTransform.transform(timeSeries);

        assertArrayEquals(expectation, transformed, 0.0001);
    }

}