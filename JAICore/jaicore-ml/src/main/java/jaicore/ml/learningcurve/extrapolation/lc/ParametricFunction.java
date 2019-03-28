package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.Map;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * This is a basic class that describes a function that can be parameterized
 * with a set of parameters.
 * 
 * @author Felix Weiland
 *
 */
public abstract class ParametricFunction implements UnivariateFunction{

	private Map<String, Double> params;

	public ParametricFunction() {
		super();
	}

	public ParametricFunction(Map<String, Double> params) {
		super();
		this.params = params;
	}

	public Map<String, Double> getParams() {
		return params;
	}

	public void setParams(Map<String, Double> params) {
		this.params = params;
	}

}
