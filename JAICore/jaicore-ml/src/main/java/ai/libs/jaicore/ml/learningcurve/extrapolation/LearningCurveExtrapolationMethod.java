package ai.libs.jaicore.ml.learningcurve.extrapolation;

import java.util.concurrent.ExecutionException;

import org.api4.java.ai.ml.LearningCurve;

/**
 * Functional interface for extrapolating a learning curve from anchorpoints.
 * 
 * @author Lukas Brandt
 */
@FunctionalInterface
public interface LearningCurveExtrapolationMethod {

	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues, int dataSetSize)
			throws InvalidAnchorPointsException, InterruptedException, ExecutionException;

}
