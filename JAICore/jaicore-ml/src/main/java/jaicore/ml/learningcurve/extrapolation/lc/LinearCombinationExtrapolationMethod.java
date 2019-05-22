package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.concurrent.ExecutionException;

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
	private static final String ENDPOINT = "/jaicore/web/api/v1/mcmc/modelparams";

	private static final String DEFAULT_HOST = "localhost";

	private static final String DEFAULT_PORT = "8080";

	private String serviceUrl;

	public LinearCombinationExtrapolationMethod() {
		this.serviceUrl = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + ENDPOINT;
	}

	public LinearCombinationExtrapolationMethod(String serviceHost, String port) {
		this.serviceUrl = "http://" + serviceHost + ":" + port + ENDPOINT;
	}

	@Override
	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues, int dataSetSize)
			throws InvalidAnchorPointsException, InterruptedException, ExecutionException {
		// Request model parameters to create learning curve
		ExtrapolationServiceClient<LinearCombinationLearningCurveConfiguration> client = new ExtrapolationServiceClient<>(
				serviceUrl, LinearCombinationLearningCurveConfiguration.class);
		LinearCombinationLearningCurveConfiguration configuration = client.getConfigForAnchorPoints(xValues, yValues);
		return new LinearCombinationLearningCurve(configuration, dataSetSize);
	}

}
