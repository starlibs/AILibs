package jaicore.ml.tsc.filter.transform;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Test suite for the {@link CosineTransform} implementation.
 * 
 * @author fischor
 */
public class CosineTransformTest {

    @Test
    public void testCorrectnessForTransformation() {
        // Input.
        double[] timeSeries = { 1, 2, 3, 4, 5 };
        // Expectation.
        double[] expectation = { 15, -4.97979657, 0, -0.4490279766, 0 };

        CosineTransform cosineTransform = new CosineTransform();
        double[] transformed = cosineTransform.transform(timeSeries);

        assertArrayEquals(expectation, transformed, 0.0001);
    }

}