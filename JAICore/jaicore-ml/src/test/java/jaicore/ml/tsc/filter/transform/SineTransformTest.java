package jaicore.ml.tsc.filter.transform;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Test suite for the {@link SineTransform} implementation.
 * 
 * @author fischor
 */
public class SineTransformTest {

    @Test
    public void testCorrectnessForTransformation() {
        // Input.
        double[] timeSeries = { 1, 2, 3, 4, 5 };
        // Expectation.
        double[] expectation = { 9.708203932, -4.253254042, 3.708203932, -2.628655561, 3 };

        SineTransform cosineTransform = new SineTransform();
        double[] transformed = cosineTransform.transform(timeSeries);

        assertArrayEquals(expectation, transformed, 0.0001);
    }

}