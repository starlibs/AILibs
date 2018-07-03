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
	private String[] values;
	private double[] indices;

	public CategoricalFeatureDomain(final String[] values) {
		super();
		this.values = values;
	}

	public CategoricalFeatureDomain(CategoricalFeatureDomain domain) {
		super();
		this.values = domain.values;
	}

	public CategoricalFeatureDomain(final Collection<String> values, int[] indices) {
		this(values.toArray(new String[] {}));
	}

	public String[] getValues() {
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

	@Override
	public boolean contains(Object item) {
		if (item == null)
			throw new IllegalArgumentException("Cannot request membership of NULL in a categorical parameter domain.");
		String itemAsString = item.toString();
		for (int i = 0; i < values.length; i++)
			if (values[i].equals(itemAsString))
				return true;
		return false;
	}

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
		return "CategoricalParameterDomain [values=" + Arrays.toString(values) + "]";
	}

	@Override
	public double getRangeSize() {
		// return values.length;
		return indices.length;
	}
	
	public void setIndices(double[] indices) {
		this.indices = indices;
	}

	@Override
	public boolean containsInstance(double value) {
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] == value)
				return true;
		}
		return false;
	}
}