package jaicore.web.mcmc.rest.message;

import java.util.Map;

public class McmcResponse {

	private Map<String, Double> weights;

	private Map<String, Map<String, Double>> parameters;

	public McmcResponse(Map<String, Double> weights, Map<String, Map<String, Double>> parameters) {
		super();
		this.weights = weights;
		this.parameters = parameters;
	}

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

}
