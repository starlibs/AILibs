package ai.libs.jaicore.ml.weka.rangequery.learner.intervaltree.featurespace;

import ai.libs.jaicore.basic.sets.Interval;
import ai.libs.jaicore.ml.weka.rangequery.learner.intervaltree.ExtendedRandomTree;

/**
 * Description of a numeric feature domain. Needed for fANOVA application in the {@link ExtendedRandomTree}.
 *
 * @author Felix Mohr, jmhansel
 *
 */
public class NumericFeatureDomain extends FeatureDomain {

	private static final long serialVersionUID = 7137323856374433244L;
	private final Interval interval;

	public NumericFeatureDomain(final boolean isInteger, final double min, final double max) {
		super();
		this.interval = new Interval(isInteger, min, max);
	}

	public NumericFeatureDomain(final NumericFeatureDomain domain) {
		this(domain.isInteger(), domain.getMin(), domain.getMax());
	}

	public boolean isInteger() {
		return this.interval.isInteger();
	}

	public double getMin() {
		return this.interval.getMin();
	}

	public double getMax() {
		return this.interval.getMax();
	}

	public void setMin(final double min) {
		this.interval.setMin(min);
	}

	public void setMax(final double max) {
		this.interval.setMax(max);
	}

	@Override
	public String toString() {
		return "NumericFeatureDomain [isInteger=" + this.isInteger() + ", min=" + this.getMin() + ", max=" + this.getMax() + "]";
	}

	@Override
	public boolean contains(final Object item) {
		return this.interval.contains(item);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.interval == null) ? 0 : this.interval.hashCode());
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
		if (this.interval == null) {
			if (other.interval != null) {
				return false;
			}
		} else if (!this.interval.equals(other.interval)) {
			return false;
		}
		return true;
	}

	@Override
	public double getRangeSize() {
		double temp = this.getMax() - this.getMin();
		// For safety, if the interval is empty, it shouldn't effect the range size of the feature space
		if (temp == 0.0d) {
			return 1.0d;
		}
		return temp;
	}

	@Override
	public boolean containsInstance(final double value) {
		return this.contains(value);
	}

	@Override
	public String compactString() {
		return "[" + this.getMin() + "," + this.getMax() + "]";
	}
}