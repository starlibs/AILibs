package jaicore.ml.interfaces;

/**
 * Interface for the result of an learning curve extrapolation. Has to save the
 * data necessary for representing the curve and give information about the
 * values.
 * 
 * @author Lukas Brandt
 */
public interface LearningCurve {

	/**
	 * Calculated or search a saturation point with a tolerance of epsilon.
	 * 
	 * @param epsilon
	 *            Epsilon value(must be > 0) that is a tolerance the saturation
	 *            point can deviate.
	 * @return Saturation point with tolerated deviation.
	 */
	public double getSaturationPoint(double epsilon);

	/**
	 * Calculates or looks-up the curves value at a given point.
	 * 
	 * @param x
	 *            The x value of the point.
	 * @return The y value of the curve at the given x value.
	 */
	public double getCurveValue(double x);

	/**
	 * Calculates or looks-up the value of the derivative of the learning point at a
	 * given point.
	 * 
	 * @param x
	 *            The x value of the point.
	 * @return The y value of the derivative at the given x value.
	 */
	public double getDerivativeCurveValue(double x);

	/**
	 * Calculates or looks-up the value the learning curve converges to. In other
	 * words, the convergence value is the value that would be achieved if
	 * sufficiently much data would be available.
	 * 
	 * @return
	 */
	public double getConvergenceValue();

}
