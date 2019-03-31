package jaicore.ml.tsc.filter.derivate;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Test suite for the
 * {@link jaicore.ml.tsc.filter.derivate.ForwardDifferenceDerivate}
 * implementation.
 * 
 * @author fischor
 */
public class ForwardDifferenceDerivateTest {

    /**
     * Correctness test. Tests the derivate calculation (without boundaries) based
     * on an defined input and expected output.
     */
    @Test
    public void testCorrectnessForDerivateCalculation() {
        // Input.
        double[] timeSeries = { 1, 2, 3, 4, 5 };
        // Expectation.
        double[] expectation = { -1, -1, -1, -1 };

        ForwardDifferenceDerivate ForwardDifferenceDerivate = new ForwardDifferenceDerivate();
        double[] derivate = ForwardDifferenceDerivate.transform(timeSeries);

        assertArrayEquals(expectation, derivate, 0);
    }

    /**
     * Correctness test. Tests the derivate calculation with boundaries based on an
     * defined input and expected output.
     */
    @Test
    public void testCorrectnessForDerivateWithBoundariesCalculation() {
        // Input.
        double[] timeSeries = { 1, 3, 3, 6, 5 };
        // Expectation.
        double[] expectation = { 2, 0, 3, -1, -1 };

        ForwardDifferenceDerivate ForwardDifferenceDerivate = new ForwardDifferenceDerivate(true);
        double[] derivate = ForwardDifferenceDerivate.transform(timeSeries);

        assertArrayEquals(expectation, derivate, 0);
    }

}