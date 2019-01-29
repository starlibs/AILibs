package jaicore.ml.learningcurve.extrapolation;

import jaicore.ml.interfaces.LearningCurve;

/**
 * Functional interface for extrapolating a learning curve from anchorpoints.
 * 
 * @author Lukas Brandt
 */
@FunctionalInterface
public interface LearningCurveExtrapolationMethod {

	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues)
			throws InvalidAnchorPointsException;

}
