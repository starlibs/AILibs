package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.Map;

/**
 * This class encapsulates all parameters that are required in order to create a
 * weighted linear combination of parameterized functions. Hence, a
 * LinearCombinationConfiguration object contains a weight for each function
 * and, for each function, a map which maps parameter names to its values.
 * 
 * @author Felix Weiland
 *
 */
public class LinearCombinationConfiguration {

	/** Weights of the functions */
	private Map<String, Double> weights;

	/** Individual function parameters */
	private Map<String, Map<String, Double>> parameters;

	public Map<String, Double> getWeights() {
		return weights;
	}

	public void setWeights(Map<String, Double> weights) {
		this.weights = weights;
	}

	public Map<String, Map<String, Double>> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Map<String, Double>> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "LinearCombinationConfiguration [weights=" + weights + ", parameters=" + parameters + "]";
	}

}
