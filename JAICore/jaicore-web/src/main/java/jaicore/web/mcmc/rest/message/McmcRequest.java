package jaicore.web.mcmc.rest.message;

import java.util.List;

public class McmcRequest {

	private List<Integer> xValues;

	private List<Double> yValues;

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

	@Override
	public String toString() {
		return "McmcRequest [xValues=" + xValues + ", yValues=" + yValues + "]";
	}

}
