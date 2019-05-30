package jaicore.ml.learningcurve.extrapolation.client;

import java.util.List;

/**
 * This class describes the request that is sent to an Extrapolation Service. It
 * contains the x- and y-values of the anchor points.
 *
 * @author Felix Weiland
 *
 */
public class ExtrapolationRequest {

	private List<Integer> xValues;

	private List<Double> yValues;

	private Integer numSamples;

	public List<Integer> getxValues() {
		return this.xValues;
	}

	public void setxValues(final List<Integer> xValues) {
		this.xValues = xValues;
	}

	public List<Double> getyValues() {
		return this.yValues;
	}

	public void setyValues(final List<Double> yValues) {
		this.yValues = yValues;
	}

	public Integer getNumSamples() {
		return this.numSamples;
	}

	public void setNumSamples(final Integer numSamples) {
		this.numSamples = numSamples;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.numSamples == null) ? 0 : this.numSamples.hashCode());
		result = prime * result + ((this.xValues == null) ? 0 : this.xValues.hashCode());
		result = prime * result + ((this.yValues == null) ? 0 : this.yValues.hashCode());
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
		ExtrapolationRequest other = (ExtrapolationRequest) obj;
		if (this.numSamples == null) {
			if (other.numSamples != null) {
				return false;
			}
		} else if (!this.numSamples.equals(other.numSamples)) {
			return false;
		}
		if (this.xValues == null) {
			if (other.xValues != null) {
				return false;
			}
		} else if (!this.xValues.equals(other.xValues)) {
			return false;
		}
		if (this.yValues == null) {
			if (other.yValues != null) {
				return false;
			}
		} else if (!this.yValues.equals(other.yValues)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExtrapolationRequest [xValues=" + this.xValues + ", yValues=" + this.yValues + ", numSamples=" + this.numSamples + "]";
	}

}
