package jaicore.ml.learningcurve.extrapolation;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface for the result of an learning curve extrapolation. Has to save the
 * data necessary for representing the curve and give information about the
 * values.
 * 
 * @author Lukas Brandt
 */
public interface ExtrapolatedLearningcurve {

	/**
	 * Calculated or search an interval around saturation point, i. e. the point where the curve
	 * starts to flatten.
	 * 
	 * @param epsilon Epsilon value(must be > 0) that the prediction of the
	 *                saturation point is allowed to deviate.
	 * @return Left and right border of the interval, where the saturation point will be inside of.
	 */
	public Pair<Double, Double> getSaturationPoint(double epsilon);

	/**
	 * Calculates or looks-up the curves value at a given point.
	 * 
	 * @param x The x value of the point.
	 * @return The y value of the curve at the given x value.
	 */
	public double getCurveValue(double x);

}
