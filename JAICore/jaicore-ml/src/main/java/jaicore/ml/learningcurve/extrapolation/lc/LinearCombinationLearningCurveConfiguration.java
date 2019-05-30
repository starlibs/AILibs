package jaicore.ml.learningcurve.extrapolation.lc;

import java.util.List;

/**
 * A configuration for a linear combination learning curve consists of
 * parameterizations for at least one linear combination function. If multiple
 * parameterizations are given, the value of the learning curve can be
 * calculated by averaging the values of the individual linear combination
 * functions.
 *
 * @author Felix Weiland
 *
 */
public class LinearCombinationLearningCurveConfiguration {

	private List<LinearCombinationParameterSet> parameterSets;

	public List<LinearCombinationParameterSet> getParameterSets() {
		return this.parameterSets;
	}

	public void setParameterSets(final List<LinearCombinationParameterSet> parameterSets) {
		this.parameterSets = parameterSets;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.parameterSets == null) ? 0 : this.parameterSets.hashCode());
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
		LinearCombinationLearningCurveConfiguration other = (LinearCombinationLearningCurveConfiguration) obj;
		if (this.parameterSets == null) {
			if (other.parameterSets != null) {
				return false;
			}
		} else if (!this.parameterSets.equals(other.parameterSets)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "LinearCombinationLearningCurveConfiguration [parameterSets=" + this.parameterSets + "]";
	}

}
