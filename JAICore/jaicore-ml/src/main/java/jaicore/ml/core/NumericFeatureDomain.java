package jaicore.ml.core;

import weka.core.Instance;

/**
 * Description of a numeric feature domain. Needed for fANOVA application in the
 * {@link ExtendedRandomTree}.
 * 
 * @author jmhansel
 *
 */
public class NumericFeatureDomain extends FeatureDomain {
	private final boolean isInteger;
	private double min, max;

	public NumericFeatureDomain(final boolean isInteger, final double min, final double max) {
		super();
		this.isInteger = isInteger;
		this.min = min;
		this.max = max;
	}

	public NumericFeatureDomain(NumericFeatureDomain domain) {
		super();
		this.isInteger = domain.isInteger;
		this.min = domain.min;
		this.max = domain.max;
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

	public void setMin(double min) {
		this.min = min;
	}

	public void setMax(double max) {
		this.max = max;
	}

	@Override
	public String toString() {
		return "NumericFeatureDomain [isInteger=" + isInteger + ", min=" + min + ", max=" + max + "]";
	}

	@Override
	public boolean contains(Object item) {
		if (!(item instanceof Number))
			return false;
		Double n = (Double) item;
		return n >= min && n <= max;
	}

	// TODO do I need this?
	// @Override
	// public boolean subsumes(ParameterDomain otherDomain) {
	// if (!(otherDomain instanceof NumericParameterDomain))
	// return false;
	// NumericParameterDomain otherNumericDomain =
	// (NumericParameterDomain)otherDomain;
	// if (this.isInteger && !otherNumericDomain.isInteger)
	// return false;
	// return this.min <= otherNumericDomain.getMin() && this.max >=
	// otherNumericDomain.getMax();
	// }

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
		NumericFeatureDomain other = (NumericFeatureDomain) obj;
		if (isInteger != other.isInteger)
			return false;
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
			return false;
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
			return false;
		return true;
	}

	@Override
	public double getRangeSize() {
		double temp = max - min;
		// For safety, if the interval is empty, it shouldn't effect the range size of
		// the feature space
		if (temp == 0.0d)
			return 1.0d;
		return temp;
	}

	@Override
	public boolean containsInstance(double value) {
		// TODO
		// System.out.println("Check: " + min + " <= " + value + " <= " + max + " " +
		// ((value >= min) && (value <= max)));
		return ((value >= min) && (value <= max));
	}

	@Override
	public String compactString() {
		String result = "[" + this.min + "," + this.max + "]";
		return result;
	}
}