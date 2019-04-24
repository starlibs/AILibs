package jaicore.ml.learningcurve.extrapolation.ipl;

import java.util.concurrent.ExecutionException;

import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.client.ExtrapolationServiceClient;

/**
 * This class describes a method for learning curve extrapolation which
 * generates an Inverse Power Law function. The parameter of this function are
 * predicted in an external component that is called via HTTP.
 * 
 * @author Lukas Brandt
 *
 */
public class InversePowerLawExtrapolationMethod implements LearningCurveExtrapolationMethod {

	// We assume the service to be running locally
	private static final String ENDPOINT = "/jaicore/web/api/v1/ipl/modelparams";

	private static final String DEFAULT_HOST = "localhost";

	private static final String DEFAULT_PORT = "8081";

	private String serviceUrl;

	public InversePowerLawExtrapolationMethod() {
		this.serviceUrl = "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT + ENDPOINT;
	}

	public InversePowerLawExtrapolationMethod(String serviceHost, String port) {
		this.serviceUrl = "http://" + serviceHost + ":" + port + ENDPOINT;
	}

	@Override
	public LearningCurve extrapolateLearningCurveFromAnchorPoints(int[] xValues, double[] yValues, int dataSetSize)
			throws InvalidAnchorPointsException, InterruptedException, ExecutionException {
		// Request model parameters to create learning curve
		ExtrapolationServiceClient<InversePowerLawConfiguration> client = new ExtrapolationServiceClient<>(serviceUrl,
				InversePowerLawConfiguration.class);
		InversePowerLawConfiguration configuration = client.getConfigForAnchorPoints(xValues, yValues);
		configuration.setA(Math.max(0.00000000001, Math.min(configuration.getA(), 0.99999999999)));
		configuration.setC(Math.max(-0.99999999999, Math.min(configuration.getC(), -0.00000000001)));
		return new InversePowerLawLearningCurve(configuration);
	}

}
