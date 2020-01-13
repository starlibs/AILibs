package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.lc;

import java.util.concurrent.ExecutionException;

import org.api4.java.ai.ml.core.evaluation.learningcurve.ILearningCurve;

import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.InvalidAnchorPointsException;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.client.ExtrapolationServiceClient;

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
	public ILearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues, int dataSetSize)
			throws InvalidAnchorPointsException, InterruptedException, ExecutionException {
		// Request model parameters to create learning curve
		ExtrapolationServiceClient<LinearCombinationLearningCurveConfiguration> client = new ExtrapolationServiceClient<>(
				serviceUrl, LinearCombinationLearningCurveConfiguration.class);
		LinearCombinationLearningCurveConfiguration configuration = client.getConfigForAnchorPoints(xValues, yValues);
		return new LinearCombinationLearningCurve(configuration, dataSetSize);
	}

}
