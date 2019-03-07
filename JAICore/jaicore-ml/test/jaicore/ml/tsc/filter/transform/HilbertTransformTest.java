package jaicore.ml.tsc.filter.transform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.Test;

/**
 * Test suite for the {@link HilbertTransform} implementation.
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

}