package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.Map;

/**
 * This class encapsulates all parameters that are required in order to create a
 * weighted linear combination of parameterized functions. Hence, a
 * LinearCombinationParameterSet object contains a weight for each function and,
 * for each function, a map which maps parameter names to its values.
 *
 * @author Felix Weiland
 *
 */
public class LinearCombinationParameterSet {

	/** Weights of the functions */
	private Map<String, Double> weights;

	/** Individual function parameters */
	private Map<String, Map<String, Double>> parameters;

	public Map<String, Double> getWeights() {
		return this.weights;
	}

	public void setWeights(final Map<String, Double> weights) {
		this.weights = weights;
	}

	public Map<String, Map<String, Double>> getParameters() {
		return this.parameters;
	}

	public void setParameters(final Map<String, Map<String, Double>> parameters) {
		this.parameters = parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		result = prime * result + ((this.weights == null) ? 0 : this.weights.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		LinearCombinationParameterSet other = (LinearCombinationParameterSet) obj;
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		if (this.weights == null) {
			if (other.weights != null) {
				return false;
			}
		} else if (!this.weights.equals(other.weights)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "LinearCombinationParameterSet [weights=" + this.weights + ", parameters=" + this.parameters + "]";
	}

}
