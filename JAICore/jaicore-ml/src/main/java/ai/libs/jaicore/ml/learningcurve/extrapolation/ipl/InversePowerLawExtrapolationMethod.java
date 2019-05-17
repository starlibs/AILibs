package ai.libs.jaicore.ml.learningcurve.extrapolation.ipl;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.ml.interfaces.LearningCurve;
import ai.libs.jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import ai.libs.jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.learningcurve.extrapolation.client.ExtrapolationServiceClient;

/**
 * This class describes a method for learning curve extrapolation which
 * generates an Inverse Power Law function. The parameter of this function are
 * predicted in an external component that is called via HTTP.
 * 
 * @author Lukas Brandt
 *
 */
public class InversePowerLawExtrapolationMethod implements LearningCurveExtrapolationMethod, ILoggingCustomizable {
	
	private Logger logger = LoggerFactory.getLogger(InversePowerLawExtrapolationMethod.class);

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
		logger.info("Querying service {} to extrapolate learning curve at anchor points {} with values {}", serviceUrl, Arrays.toString(xValues), Arrays.toString(yValues));
		ExtrapolationServiceClient<InversePowerLawConfiguration> client = new ExtrapolationServiceClient<>(serviceUrl,
				InversePowerLawConfiguration.class);
		InversePowerLawConfiguration configuration = client.getConfigForAnchorPoints(xValues, yValues);
		configuration.setA(Math.max(0.00000000001, Math.min(configuration.getA(), 0.99999999999)));
		configuration.setC(Math.max(-0.99999999999, Math.min(configuration.getC(), -0.00000000001)));
		return new InversePowerLawLearningCurve(configuration);
	}

	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	@Override
	public void setLoggerName(String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

}
