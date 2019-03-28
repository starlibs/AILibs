package jaicore.ml.learningcurve.extrapolation.ipl;

import java.math.BigDecimal;

import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;

import jaicore.ml.interfaces.AnalyticalLearningCurve;

/**
 * Representation of a learning curve with the Inverse Power Law function, which
 * has three parameters named a, b and c. The function is f(x) = (1-a) - b *
 * x^c. O
 * 
 * @author Lukas Brandt
 *
 */
public class InversePowerLawLearningCurve implements AnalyticalLearningCurve {

	private double a, b, c;

	public InversePowerLawLearningCurve(double a, double b, double c) {
		assert a > 0 && a < 1;
		assert c > -1 && c < 0;
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public InversePowerLawLearningCurve(InversePowerLawConfiguration configuration) {
		assert configuration.getA() > 0 && configuration.getA() < 1;
		assert configuration.getC() > -1 && configuration.getC() < 0;
		this.a = configuration.getA();
		this.b = configuration.getB();
		this.c = configuration.getC();
	}

	@Override
	public double getSaturationPoint(double epsilon) {
		assert epsilon > 0;
		double n = this.c - 1.0d;
		double base = -(epsilon / (this.b * this.c));
		double result = Math.pow(Math.E, Math.log(base) / n);
		return result;
	}

	@Override
	public double getCurveValue(double x) {
		return (1.0d - this.a) - this.b * Math.pow(x, this.c);
	}

	@Override
	public double getDerivativeCurveValue(double x) {
		return (-this.b) * this.c * Math.pow(x, this.c - 1.0d);
	}

	@Override
	public String toString() {
		return "(1 - " + new BigDecimal(this.a).toPlainString() + ") - " + new BigDecimal(this.b).toPlainString()
				+ " * x ^ " + new BigDecimal(this.c).toPlainString();
	}

	@Override
	public double getConvergenceValue() {
		UnivariateSolver solver = new BrentSolver(0, 1.0d);
		double convergencePoint = -1;
		int upperIntervalBound = 10000;
		int retries_left = 8;
		while (retries_left > 0 && convergencePoint == -1) {
			try {
				convergencePoint = solver.solve(1000, (x) -> this.getDerivativeCurveValue(x) - 0.0000001, 1,
						upperIntervalBound);
			} catch (NoBracketingException e) {
				System.out.println(e.getMessage());
				retries_left--;
				upperIntervalBound *= 2;
			}
		}
		if (convergencePoint == -1) {
			throw new RuntimeException(
					String.format("No solution could be found in interval [1,%d]", upperIntervalBound));
		}
		return this.getCurveValue(convergencePoint);
	}

	public double getA() {
		return this.a;
	}

	public double getB() {
		return this.b;
	}

	public double getC() {
		return this.c;
	}

}
