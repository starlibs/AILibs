package hasco.model;

public class NumericParameterDomain extends ParameterDomain {
	private final boolean isInteger;
	private final double min, max;

	public NumericParameterDomain(final boolean isInteger, final double min, final double max) {
		super();
		this.isInteger = isInteger;
		this.min = min;
		this.max = max;
	}

	public boolean isInteger() {
		return this.isInteger;
	}

	public double getMin() {
		return this.min;
	}

	public double getMax() {
		return this.max;
	}

	@Override
	public String toString() {
		return "NumericParameterDomain [isInteger=" + isInteger + ", min=" + min + ", max=" + max + "]";
	}

	@Override
	public boolean contains(Object item) {
		if (!(item instanceof Number))
			return false;
		Double n = (Double)item;
		return n >= min && n <= max;
	}

	@Override
	public boolean subsumes(ParameterDomain otherDomain) {
		if (!(otherDomain instanceof NumericParameterDomain))
			return false;
		NumericParameterDomain otherNumericDomain = (NumericParameterDomain)otherDomain;
		if (this.isInteger && !otherNumericDomain.isInteger)
			return false;
		return this.min <= otherNumericDomain.getMin() && this.max >= otherNumericDomain.getMax();
	}
}
