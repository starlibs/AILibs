package jaicore.ml.learningcurve.extrapolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawLearningCurve;

public class InversePowerLawLearningCurveTest {

	@Test
	public void testValueCalculation() {
		int[] xValues = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		double[] yValues = new double[] {
				0.35d,
				0.47d,
				0.52d,
				0.56d,
				0.58d,
				0.60d,
				0.62d,
				0.63d,
				0.64d,
				0.65d
		};
		InversePowerLawLearningCurve curve = new InversePowerLawLearningCurve(0.15d, 0.5d, -0.4d);
		for (int i = 0; i < xValues.length; i++) {
			assertEquals(yValues[i], Math.floor(curve.getCurveValue(xValues[i]) * 100) / 100.0d);
		}
	}
	
	@Test
	public void testDerivativeValueCalculation() {
		int[] xValues = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		double[] yValues = new double[] {
				0.2d,
				0.07d,
				0.04d,
				0.02d,
				0.02d,
				0.01d,
				0.01d,
				0.01d,
				0.00d,
				0.00d
		};
		InversePowerLawLearningCurve curve = new InversePowerLawLearningCurve(0.15d, 0.5d, -0.4d);
		for (int i = 0; i < xValues.length; i++) {
			assertEquals(yValues[i], Math.floor(curve.getDerivativeCurveValue(xValues[i]) * 100) / 100.0d);
		}
	}
	
	@Test
	public void testSaturaionPointCalculation() {
		double[] epsilonValues = new double[] {
				1d,
				0.05d,
				0.01d,
				0.001d
		};
		double[] expectedSaturationPoints = new double[] {
				0.31d,
				2.69d,
				8.49d,
				44.01d
		};
		InversePowerLawLearningCurve curve = new InversePowerLawLearningCurve(0.15d, 0.5d, -0.4d);
		for (int i = 0; i < epsilonValues.length; i++) {
			assertEquals(expectedSaturationPoints[i], Math.floor(curve.getSaturationPoint(epsilonValues[i]) * 100) / 100.0d);
		}
	}
	
	@Test
	public void testConvergenceValueCalculation() {
		InversePowerLawLearningCurve curve = new InversePowerLawLearningCurve(0.15d, 0.5d, -0.4d);
		double convergenceValue = curve.getConvergenceValue();
		System.out.println(convergenceValue);
		assertTrue(convergenceValue > 0.8 && convergenceValue < 0.9);
	}

}
