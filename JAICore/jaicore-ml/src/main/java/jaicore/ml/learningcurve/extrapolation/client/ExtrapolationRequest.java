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
		return xValues;
	}

	public void setxValues(List<Integer> xValues) {
		this.xValues = xValues;
	}

	public List<Double> getyValues() {
		return yValues;
	}

	public void setyValues(List<Double> yValues) {
		this.yValues = yValues;
	}

	public Integer getNumSamples() {
		return numSamples;
	}

	public void setNumSamples(Integer numSamples) {
		this.numSamples = numSamples;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((numSamples == null) ? 0 : numSamples.hashCode());
		result = prime * result + ((xValues == null) ? 0 : xValues.hashCode());
		result = prime * result + ((yValues == null) ? 0 : yValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtrapolationRequest other = (ExtrapolationRequest) obj;
		if (numSamples == null) {
			if (other.numSamples != null) {
				return false;
			}
		} else if (!numSamples.equals(other.numSamples)) {
			return false;
		}
		if (xValues == null) {
			if (other.xValues != null) {
				return false;
			}
		} else if (!xValues.equals(other.xValues)) {
			return false;
		}
		if (yValues == null) {
			if (other.yValues != null) {
				return false;
			}
		} else if (!yValues.equals(other.yValues)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExtrapolationRequest [xValues=" + xValues + ", yValues=" + yValues + ", numSamples=" + numSamples + "]";
	}

}
