package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation;

import java.util.concurrent.ExecutionException;

import org.api4.java.ai.ml.core.evaluation.learningcurve.ILearningCurve;

/**
 * Functional interface for extrapolating a learning curve from anchorpoints.
 * 
 * @author Lukas Brandt
 */
@FunctionalInterface
public interface LearningCurveExtrapolationMethod {

	public ILearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues, int dataSetSize)
			throws InvalidAnchorPointsException, InterruptedException, ExecutionException;

}
