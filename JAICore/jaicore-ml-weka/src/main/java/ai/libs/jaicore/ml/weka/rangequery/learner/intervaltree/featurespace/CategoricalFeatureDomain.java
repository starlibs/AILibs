package ai.libs.jaicore.ml.weka.rangequery.learner.intervaltree.featurespace;

import java.util.Arrays;

import ai.libs.jaicore.ml.weka.rangequery.learner.intervaltree.ExtendedRandomTree;

/**
 * Description of a categorical feature domain. Needed for fANOVA application in
 * the {@link ExtendedRandomTree}.
 *
 * @author jmhansel
 *
 */
public class CategoricalFeatureDomain extends FeatureDomain {
	/**
	 * Automatically generated version uid for serialization.
	 */
	private static final long serialVersionUID = 5890074168706122933L;

	private double[] values;

	public CategoricalFeatureDomain(final double[] values) {
		super();
		this.values = values;
	}

	public CategoricalFeatureDomain(final CategoricalFeatureDomain domain) {
		super();
		this.values = domain.values;
	}

	public double[] getValues() {
		return this.values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.values);
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
		CategoricalFeatureDomain other = (CategoricalFeatureDomain) obj;
		return Arrays.equals(this.values, other.values);
	}

	@Override
	public String toString() {
		return "CategoricalFeatureDomain [values=" + Arrays.toString(this.values) + "]";
	}

	@Override
	public double getRangeSize() {
		// For safety, if the domain is empty, it shouldn't effect the range size of the feature space
		if (this.values.length == 0) {
			return 1;
		}
		return this.values.length;
	}

	public void setValues(final double[] values) {
		this.values = values;
	}

	@Override
	public boolean containsInstance(final double value) {
		for (int i = 0; i < this.values.length; i++) {
			if (this.values[i] == value) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(final Object item) {
		double value = (double) item;
		for (int i = 0; i < this.values.length; i++) {
			if (this.values[i] == value) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String compactString() {
		return "yet to implement";
	}
}