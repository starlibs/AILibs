package hasco.model;

public class NumericParameter extends Parameter {
	private final boolean isInteger;
	private final double min, max;
	
	public NumericParameter(String name, boolean isInteger, double defaultValue, double min, double max) {
		super(name, defaultValue);
		this.isInteger = isInteger;
		this.min = min;
		this.max = max;
	}

	public boolean isInteger() {
		return isInteger;
	}
	
	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}
}
