package jaicore.ml.learningcurve.extrapolation.ipl;

/**
 * This class encapsulates the three parameters that are required in order to
 * create a Inverse Power Law function.
 * 
 * @author Lukas Brandt
 *
 */
public class InversePowerLawConfiguration {

	// Inverse Power Law parameters
	private double a;
	private double b;
	private double c;

	public double getA() {
		return this.a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public double getB() {
		return this.b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public double getC() {
		return this.c;
	}

	public void setC(double c) {
		this.c = c;
	}

	@Override
	public String toString() {
		return "InversePowerLawConfiguration [a=" + this.a + ", b=" + this.b + ", c=" + this.c + "]";
	}
}
