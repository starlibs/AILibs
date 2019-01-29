package jaicore.ml.learningcurve.extrapolation.InversePowerLaw;

import jaicore.ml.interfaces.LearningCurve;

/**
 * Representation of a learning curve with the Inverse Power Law function, which
 * has three parameters named a, b and c. The function is f(x) = (1-a) - b *
 * x^c.
 * 
 * @author Lukas Brandt
 *
 */
public class InversePowerLawLearningCurve implements LearningCurve {

	private double a, b, c;

	public InversePowerLawLearningCurve(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public double getSaturationPoint(double epsilon) {
		assert epsilon > 0;
		double n = this.c - 1.0d;
		double base = - (epsilon / (this.b * this.c));
		double result = Math.pow(Math.E, Math.log(base)/n);
		return result;
	}


	@Override
	public double getCurveValue(double x) {
		return (1.0d - this.a) - this.b * Math.pow(x, this.c);
	}

}
