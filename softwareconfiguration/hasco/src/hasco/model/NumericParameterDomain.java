package hasco.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NumericParameterDomain extends ParameterDomain {
	private final boolean isInteger;
	private final double min, max;

	@JsonCreator
	public NumericParameterDomain(@JsonProperty("integer") final boolean isInteger, @JsonProperty("min")final double min, @JsonProperty("maxf")final double max) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isInteger ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		NumericParameterDomain other = (NumericParameterDomain) obj;
		if (isInteger != other.isInteger)
			return false;
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
			return false;
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
			return false;
		return true;
	}
}
