package jaicore.ml.dyadranking.optmization;

import java.util.Arrays;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.optimizing.IGradientDescendableFunction;
import jaicore.ml.dyadranking.optimizing.IGradientFunction;

/**
 * Test data for the parameterized LFBGS test. The function is the function
 * whose coefficients should be guessed
 * 
 * @author elppa
 *
 */
public class LFBGSTestData {

	private IGradientDescendableFunction function;

	private IGradientFunction gradient;

	private double[] expectedResult;

	public double[] getExpectedResult() {
		return expectedResult;
	}

	public LFBGSTestData(IGradientDescendableFunction function, IGradientFunction gradient, double[] expectedResult) {
		super();
		this.function = function;
		this.gradient = gradient;
		this.expectedResult = expectedResult;
	}

	public static LFBGSTestData polynomialFromCoeffs(double[] coeffs, double [] expectedResult) {
		// numerically derive the gradient
		double[] gradientCoeffs = new double[coeffs.length - 1];
		for (int i = coeffs.length - 1; i >= 1; i--) {
			double polynomialCoeff = coeffs[i];
			gradientCoeffs[i - 1] = polynomialCoeff * i;
		}
		return new LFBGSTestData(new PolynomialFunction(coeffs), new PolynomialGradientFunction(gradientCoeffs),
				coeffs);
	}

	public IGradientDescendableFunction getFunction() {
		return function;
	}

	public IGradientFunction getGradient() {
		return gradient;
	}


	static class PolynomialFunction implements IGradientDescendableFunction {

		private double[] coeffs;

		public PolynomialFunction(double[] coeffs) {
			this.coeffs = coeffs;
		}

		@Override
		public double apply(Vector vector) {
			if (vector.length() != 1)
				throw new IllegalArgumentException(
						"Input mismatch! The length of the input vector does not match the amount of variables in this polynomial. [ 1 expected but got "
								+ vector.length() + "]");
			double result = 0;
			for (int i = 0; i < coeffs.length; i++) {
				result += coeffs[i] * Math.pow(vector.getValue(0), i);
			}
			return result;
		}

	}

	static class PolynomialGradientFunction implements IGradientFunction {

		private double[] gradientCoeffs;

		public PolynomialGradientFunction(double[] gradientCoeffs) {
			this.gradientCoeffs = gradientCoeffs;
		}

		@Override
		public Vector apply(Vector vector) {
			if (vector.length() != 1)
				throw new IllegalArgumentException(
						"Input mismatch! The length of the input vector does not match the degree of this polynomial. ["
								+ gradientCoeffs.length + " expected but got " + vector.length() + "]");
			double result = 0;
			for (int i = 0; i < gradientCoeffs.length; i++) {
				result += gradientCoeffs[i] * Math.pow(vector.getValue(0), i);
			}
			return new DenseDoubleVector(new double[] { result });
		}

	}

}
