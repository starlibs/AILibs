package jaicore.ml.dyadranking.optmization;

import java.util.Arrays;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.optimizing.IGradientDescendableFunction;
import jaicore.ml.dyadranking.optimizing.IGradientFunction;

/**
 * Test data for the parameterized LFBGS test.
 * 
 * @author Mirko
 *
 */
public class LBFGSTestData {

	private IGradientDescendableFunction function;

	private IGradientFunction gradient;

	private double[] expectedResult;

	public double[] getExpectedResult() {
		return expectedResult;
	}

	/**
	 * Returns an TestDataInstance that can be used in a run of the
	 * {@link LBFGSTest}.
	 * 
	 * @param function
	 *            the function that should be minimized.
	 * @param gradient
	 *            the gradient of the functions
	 * @param expectedResult
	 *            the result that should be produced.
	 */
	public LBFGSTestData(IGradientDescendableFunction function, IGradientFunction gradient, double[] expectedResult) {
		super();
		this.function = function;
		this.gradient = gradient;
		this.expectedResult = expectedResult;
	}

	/**
	 * Creates a TestData instance that minimizes the polynomial described by this
	 * coefficients.
	 * 
	 * @param coeffs
	 *            the coefficients of the polynomial in ascending order (e.g. a_0 +
	 *            a_1 * x + a_2* x^2, ...; where a_0, ... are the provided
	 *            coefficients.)
	 * @param expectedResult
	 *            the real minimum
	 * @return
	 */
	public static LBFGSTestData polynomialFromCoeffs(double[] coeffs, double[] expectedResult) {
		// numerically derive the gradient
		double[] gradientCoeffs = new double[coeffs.length - 1];
		for (int i = coeffs.length - 1; i >= 1; i--) {
			double polynomialCoeff = coeffs[i];
			gradientCoeffs[i - 1] = polynomialCoeff * i;
		}
		return new LBFGSTestData(new PolynomialFunction(coeffs), new PolynomialGradientFunction(gradientCoeffs),
				expectedResult);
	}

	public IGradientDescendableFunction getFunction() {
		return function;
	}

	public IGradientFunction getGradient() {
		return gradient;
	}

	/**
	 * Simple wrapper for polynomials in our interfaces.
	 * 
	 * @author Mirko
	 *
	 */
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

	/**
	 * Gradients of polynomials based on the polynomial implementation
	 * 
	 * @author Mirko
	 *
	 */
	static class PolynomialGradientFunction implements IGradientFunction {

		private PolynomialFunction gradientFunct;

		public PolynomialGradientFunction(double[] gradientCoeffs) {
			this.gradientFunct = new PolynomialFunction(gradientCoeffs);
		}

		@Override
		public Vector apply(Vector vector) {
			double result = gradientFunct.apply(vector);
			return new DenseDoubleVector(new double[] { result });
		}

	}

}
