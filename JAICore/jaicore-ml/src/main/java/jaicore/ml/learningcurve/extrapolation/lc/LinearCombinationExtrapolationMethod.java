package jaicore.ml.learningcurve.extrapolation.lc;

import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.client.ExtrapolationServiceClient;

/**
 * This class describes a method for learning curve extrapolation which
 * generates a linear combination of suitable functions. The parameter of these
 * functions as well as the weighting of the are sampled via MCMC. The actual
 * MCMC sampling is done in an external component that is called via HTTP.
 * 
 * @author Felix Weiland
 *
 */
public class LinearCombinationExtrapolationMethod implements LearningCurveExtrapolationMethod {

	// We assume the service to be running locally
	private static final String SERVICE_URL = "http://localhost:8080/jaicore/web/api/v1/mcmc/modelparams";

	@Override
	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues, int dataSetSize)
			throws InvalidAnchorPointsException {
		// Request model parameters to create learning curve
		ExtrapolationServiceClient<LinearCombinationLearningCurveConfiguration> client = new ExtrapolationServiceClient<>(
				SERVICE_URL, LinearCombinationLearningCurveConfiguration.class);
		LinearCombinationLearningCurveConfiguration configuration = client.getConfigForAnchorPoints(xValues, yValues);
		return new LinearCombinationLearningCurve(configuration, dataSetSize);
	}

}
