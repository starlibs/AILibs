package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * This is a basic class that describes a function which is a weighted
 * combination of individual functions.
 * 
 * @author Felix Weiland
 *
 */
public class LinearCombinationFunction implements UnivariateFunction {

	/** Functions the linear combination consists of */
	private List<ParametricFunction> functions;

	/**
	 * Weights of the individual functions. For reasonable results, the sum of the
	 * weights should be 1.
	 */
	private List<Double> weights;

	/** Offset, which is added to the value of the linear combination */
	private double offset;

	public LinearCombinationFunction(List<ParametricFunction> functions, List<Double> weights, double offsett) {
		super();
		this.functions = functions;
		this.weights = weights;
		this.offset = offsett;
	}

	public List<ParametricFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<ParametricFunction> functions) {
		this.functions = functions;
	}

	public List<Double> getWeights() {
		return weights;
	}

	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}

	@Override
	public double value(double x) {
		double value = 0;
		for (int i = 0; i < functions.size(); i++) {
			value += functions.get(i).getValue(x) * weights.get(i);
		}
		return value + offset;
	}

}
