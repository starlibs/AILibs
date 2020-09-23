package ai.libs.jaicore.components.model;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.components.api.IParameterDomain;

public class CategoricalParameterDomain implements IParameterDomain {
	private final String[] values;

	@SuppressWarnings("unused")
	private CategoricalParameterDomain() {
		// for serialization
		this.values = null;
	}

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
		return new HashCodeBuilder().append(this.values).hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj != null && obj.getClass() == this.getClass() && Arrays.equals(this.values, ((CategoricalParameterDomain)obj).values));
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
		CategoricalParameterDomain otherCategoricalDomain = (CategoricalParameterDomain) otherDomain;
		return Arrays.asList(this.values).containsAll(Arrays.asList(otherCategoricalDomain.getValues()));
	}

	@Override
	public String toString() {
		return "CategoricalParameterDomain [values=" + Arrays.toString(this.values) + "]";
	}

	@Override
	public boolean isEquals(final Object obj0, final Object obj1) {
		return (obj0 + "").equals(obj1 + "");
	}
}
