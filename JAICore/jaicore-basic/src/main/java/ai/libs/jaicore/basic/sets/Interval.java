package ai.libs.jaicore.basic.sets;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Interval implements Serializable {

	private static final long serialVersionUID = -6928681531901708026L;
	private boolean isInteger;
	private double min;
	private double max;

	@SuppressWarnings("unused")
	private Interval() {
		// for serialization
		this.isInteger = true;
		this.min = 0;
		this.max = 0;
	}

	@JsonCreator
	public Interval(@JsonProperty("integer") final boolean isInteger, @JsonProperty("min") final double min, @JsonProperty("max") final double max) {
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

	public void setInteger(final boolean isInteger) {
		this.isInteger = isInteger;
	}

	public void setMin(final double min) {
		this.min = min;
	}

	public void setMax(final double max) {
		this.max = max;
	}

	@Override
	public String toString() {
		return "NumericParameterDomain [isInteger=" + this.isInteger + ", min=" + this.min + ", max=" + this.max + "]";
	}

	public boolean contains(final Object item) {
		if (!(item instanceof Number)) {
			return false;
		}
		Double n = (Double) item;
		boolean isInRange = n >= this.min && n <= this.max;
		return isInRange;
	}

	public boolean subsumes(final Interval otherInterval) {
		if (this.isInteger && !otherInterval.isInteger) {
			return false;
		}
		return this.min <= otherInterval.getMin() && this.max >= otherInterval.getMax();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.isInteger ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(this.max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Interval other = (Interval) obj;
		if (this.isInteger != other.isInteger) {
			return false;
		}
		if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(other.max)) {
			return false;
		}
		return Double.doubleToLongBits(this.min) == Double.doubleToLongBits(other.min);
	}
}
