package jaicore.ml.core;

import java.util.Arrays;
import java.util.Collection;

import weka.core.Instance;

/**
 * Description of a categorical feature domain. Needed for fANOVA application in
 * the {@link ExtendedRandomTree}.
 * 
 * @author jmhansel
 *
 */
public class CategoricalFeatureDomain extends FeatureDomain {
	private double[] values;

	public CategoricalFeatureDomain(final double[] values) {
		super();
		this.values = values;
	}

	public CategoricalFeatureDomain(CategoricalFeatureDomain domain) {
		super();
		this.values = domain.values;
	}

	// public CategoricalFeatureDomain(final Collection<String> values, int[]
	// indices) {
	// this(values.toArray(new String[] {}));
	// }

	public double[] getValues() {
		return this.values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(values);
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
		CategoricalFeatureDomain other = (CategoricalFeatureDomain) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	// @Override
	// public boolean contains(Object item) {
	// if (item == null)
	// throw new IllegalArgumentException("Cannot request membership of NULL in a
	// categorical parameter domain.");
	// double itemAsDouble = (double) item;
	// for (int i = 0; i < values.length; i++)
	// if (values[i].equals(itemAsDouble))
	// return true;
	// return false;
	// }

	// TODO do I need this?
	// @Override
	// public boolean subsumes(FeatureDomain otherDomain) {
	// if (!(otherDomain instanceof CategoricalFeatureDomain))
	// return false;
	// CategoricalFeatureDomain otherCategoricalDomain =
	// (CategoricalFeatureDomain)otherDomain;
	// return
	// Arrays.asList(values).containsAll(Arrays.asList(otherCategoricalDomain.getValues()));
	// }

	@Override
	public String toString() {
		return "CategoricalFeatureDomain [values=" + Arrays.toString(values) + "]";
	}

	@Override
	public double getRangeSize() {
		// For safety, if the domain is empty, it shouldn't effect the range size of the
		// feature space
		if (values.length == 0)
			return 1;
		return values.length;
	}

	public void setValues(double[] values) {
		this.values = values;
	}

	@Override
	public boolean containsInstance(double value) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == value)
				return true;
		}
		return false;
	}

	@Override
	public boolean contains(Object item) {
		double value = (double) item;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == value)
				return true;
		}
		return false;
	}

	@Override
	public String compactString() {
		return "yet to implement";
	}
}