package hasco.model;

import java.util.Arrays;
import java.util.Collection;

public class CategoricalParameterDomain implements IParameterDomain {
	private final String[] values;

	public CategoricalParameterDomain(final String[] values) {
		super();
		this.values = values;
	}

	public CategoricalParameterDomain(final Collection<String> values) {
		this(values.toArray(new String[] {}));
	}

	public String[] getValues() {
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
		CategoricalParameterDomain other = (CategoricalParameterDomain) obj;
		if (!Arrays.equals(this.values, other.values)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean contains(final Object item) {
		if (item == null) {
			throw new IllegalArgumentException("Cannot request membership of NULL in a categorical parameter domain.");
		}
		String itemAsString = item.toString();
		for (int i = 0; i < this.values.length; i++) {
			if (this.values[i].equals(itemAsString)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean subsumes(final IParameterDomain otherDomain) {
		if (!(otherDomain instanceof CategoricalParameterDomain)) {
			return false;
		}
		CategoricalParameterDomain otherCategoricalDomain = (CategoricalParameterDomain)otherDomain;
		return Arrays.asList(this.values).containsAll(Arrays.asList(otherCategoricalDomain.getValues()));
	}

	@Override
	public String toString() {
		return "CategoricalParameterDomain [values=" + Arrays.toString(this.values) + "]";
	}
}
