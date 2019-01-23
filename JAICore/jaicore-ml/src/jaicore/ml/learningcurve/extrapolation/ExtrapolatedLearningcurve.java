package jaicore.ml.learningcurve.extrapolation;

/**
 * Interface for the result of an learning curve extrapolation. Has to save the
 * data necessary for representing the curve and give information about the
 * values.
 * 
 * @author Lukas Brandt
 */
public interface ExtrapolatedLearningcurve {

	/**
	 * Calculated or search the saturation point, i. e. the point where the curve
	 * starts to flatten.
	 * 
	 * @return The x value of the point.
	 */
	public double getSaturationPoint();

	/**
	 * Calculates or looks-up the curves value at a given point.
	 * 
	 * @param x The x value of the point.
	 * @return The y value of the curve at the given x value.
	 */
	public double getCurveValue(double x);

}
