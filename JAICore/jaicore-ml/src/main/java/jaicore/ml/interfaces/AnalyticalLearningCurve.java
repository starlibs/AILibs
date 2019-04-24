package jaicore.ml.interfaces;

/**
 * Added some analytical functions to a learning curve.
 * 
 * @author Lukas Brandt
 */
public interface AnalyticalLearningCurve extends LearningCurve {

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

	/**
	 * Calculated or search a saturation point with a tolerance of epsilon.
	 * 
	 * @param epsilon
	 *            Epsilon value(must be > 0) that is a tolerance the saturation
	 *            point can deviate.
	 * @return Saturation point with tolerated deviation.
	 */
	public double getSaturationPoint(double epsilon);
}
