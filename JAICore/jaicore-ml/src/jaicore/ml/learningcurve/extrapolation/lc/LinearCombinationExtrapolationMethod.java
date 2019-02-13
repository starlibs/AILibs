package jaicore.ml.learningcurve.extrapolation.lc;

import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.lc.client.McmcServiceClient;

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

	@Override
	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues)
			throws InvalidAnchorPointsException {
		// Request model parameters to create learning curve
		McmcServiceClient client = new McmcServiceClient();
		LinearCombinationConfiguration configuration = client.getConfigForAnchorPoints(xValues, yValues);
		return new LinearCombinationLearningCurve(configuration);
	}

}
