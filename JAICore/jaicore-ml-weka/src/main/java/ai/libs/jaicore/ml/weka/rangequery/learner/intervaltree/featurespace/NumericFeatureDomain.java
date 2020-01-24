package ai.libs.jaicore.ml.weka.rangequery.learner.intervaltree.featurespace;

import ai.libs.jaicore.ml.weka.rangequery.learner.intervaltree.ExtendedRandomTree;

/**
 * Description of a numeric feature domain. Needed for fANOVA application in the
 * {@link ExtendedRandomTree}.
 *
 * @author jmhansel
 *
 */
public class NumericFeatureDomain extends FeatureDomain {
	private final boolean isInteger;
	private double min;
	private double max;

	public NumericFeatureDomain(final boolean isInteger, final double min, final double max) {
		super();
		this.isInteger = isInteger;
		this.min = min;
		this.max = max;
	}

	public NumericFeatureDomain(final NumericFeatureDomain domain) {
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

	public void setMin(final double min) {
		this.min = min;
	}

	public void setMax(final double max) {
		this.max = max;
	}

	@Override
	public String toString() {
		return "NumericFeatureDomain [isInteger=" + this.isInteger + ", min=" + this.min + ", max=" + this.max + "]";
	}

	@Override
	public boolean contains(final Object item) {
		if (!(item instanceof Number)) {
			return false;
		}
		Double n = (Double) item;
		return n >= this.min && n <= this.max;
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
		NumericFeatureDomain other = (NumericFeatureDomain) obj;
		if (this.isInteger != other.isInteger) {
			return false;
		}
		if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(other.max)) {
			return false;
		}
		return (Double.doubleToLongBits(this.min) == Double.doubleToLongBits(other.min));
	}

	@Override
	public double getRangeSize() {
		double temp = this.max - this.min;
		// For safety, if the interval is empty, it shouldn't effect the range size of the feature space
		if (temp == 0.0d) {
			return 1.0d;
		}
		return temp;
	}

	@Override
	public boolean containsInstance(final double value) {
		return ((value >= this.min) && (value <= this.max));
	}

	@Override
	public String compactString() {
		return "[" + this.min + "," + this.max + "]";
	}
}