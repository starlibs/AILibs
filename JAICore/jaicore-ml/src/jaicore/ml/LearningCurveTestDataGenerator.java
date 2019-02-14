package jaicore.ml;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.learningcurve.extrapolation.lc.LinearCombinationConfiguration;
import jaicore.ml.learningcurve.extrapolation.lc.LinearCombinationLearningCurve;
import weka.core.UnsupportedAttributeTypeException;

/**
 * 
 * @author jnowack
 * @author Felix Weiland
 *
 */
public class LearningCurveTestDataGenerator {

	public static int[] anchors = new int[] { 2, 4, 8, 16, 32, 64, 128, 256 };

	public static void main(String[] args) throws UnsupportedAttributeTypeException, InterruptedException,
			AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, Exception {

		LinearCombinationLearningCurve lc = generateFunction();

		Random rand = new Random(42);

		StringBuilder xString = new StringBuilder("x <- c(");
		StringBuilder yString = new StringBuilder("y <- c(");
		int i = 0;
		for (int anchor : anchors) {
			if (i != 0) {
				xString.append(",");
				yString.append(",");
			}
			xString.append(anchor);

			yString.append("" + (lc.getCurveValue(anchor) + rand.nextGaussian() * 0.01));
			i++;
		}
		xString.append(")");
		yString.append(")");

		System.out.println("N <- " + i);
		System.out.println(xString.toString());
		System.out.println(yString.toString());
	}

	private static LinearCombinationLearningCurve generateFunction() {
		LinearCombinationConfiguration configuration = new LinearCombinationConfiguration();
		Map<String, Double> weights = new HashMap<>();
		weights.put("pow_3", 1.0 / 6.0);
		weights.put("log_log_linear", 1.0 / 6.0);
		weights.put("log_power", 1.0 / 6.0);
		weights.put("pow_4", 1.0 / 6.0);
		weights.put("mmf", 1.0 / 6.0);
		weights.put("exp_4", 1.0 / 6.0);

		Map<String, Map<String, Double>> modelParams = new HashMap<>();

		// pow_3
		Map<String, Double> pow3Params = new HashMap<>();
		pow3Params.put("a", 4.5);
		pow3Params.put("c", 1.0);
		pow3Params.put("alpha", 0.5);
		modelParams.put("pow_3", pow3Params);

		// log log linear
		Map<String, Double> logLogLinearParams = new HashMap<>();
		logLogLinearParams.put("a", 0.35);
		logLogLinearParams.put("b", -0.05);
		modelParams.put("log_log_linear", logLogLinearParams);

		// log power
		Map<String, Double> logPowerParams = new HashMap<>();
		logPowerParams.put("a", 1.0);
		logPowerParams.put("b", 4.0);
		logPowerParams.put("c", -1.15);
		modelParams.put("log_power", logPowerParams);

		// pow_4
		Map<String, Double> pow4Params = new HashMap<>();
		pow4Params.put("a", 0.04);
		pow4Params.put("b", 0.0);
		pow4Params.put("c", 1.2);
		pow4Params.put("alpha", 0.37);
		modelParams.put("pow_4", pow4Params);

		// mmf
		Map<String, Double> mmfParams = new HashMap<>();
		mmfParams.put("alpha", 1.1);
		mmfParams.put("beta", -1.0);
		mmfParams.put("delta", 0.6);
		mmfParams.put("kappa", 0.05);
		modelParams.put("mmf", mmfParams);

		// exp_4
		Map<String, Double> exp4Params = new HashMap<>();
		exp4Params.put("a", 0.1);
		exp4Params.put("b", 0.2);
		exp4Params.put("c", 1.0);
		exp4Params.put("alpha", 0.5);
		modelParams.put("exp_4", exp4Params);

		configuration.setParameters(modelParams);
		configuration.setWeights(weights);

		LinearCombinationLearningCurve lc = new LinearCombinationLearningCurve(configuration);
		return lc;

	}
}
