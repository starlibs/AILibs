package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.Map;

/**
 * This is a basic class that describes a function that can be parameterized
 * with a set of parameters.
 * 
 * @author Felix Weiland
 *
 */
public abstract class ParametricFunction {

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

	public abstract double getValue(double x);

}
