package hasco.model;

import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoricalParameterDomain extends ParameterDomain {
	private final String[] values;

	@JsonCreator
	public CategoricalParameterDomain(@JsonProperty("values") final String[] values) {
		super();
		this.values = values;
	}

	@JsonCreator
	public CategoricalParameterDomain(@JsonProperty("values") final Collection<String> values) {
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
		CategoricalParameterDomain other = (CategoricalParameterDomain) obj;
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

	@Override
	public boolean subsumes(ParameterDomain otherDomain) {
		if (!(otherDomain instanceof CategoricalParameterDomain))
			return false;
		CategoricalParameterDomain otherCategoricalDomain = (CategoricalParameterDomain)otherDomain;
		return Arrays.asList(values).containsAll(Arrays.asList(otherCategoricalDomain.getValues()));
	}

	@Override
	public String toString() {
		return "CategoricalParameterDomain [values=" + Arrays.toString(values) + "]";
	}
}
