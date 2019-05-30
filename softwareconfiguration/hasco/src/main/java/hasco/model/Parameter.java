package hasco.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name", "defaultDomain", "defaultValue"})
public class Parameter {
	private final String name;
	private final IParameterDomain defaultDomain;
	private final Object defaultValue;

	@JsonCreator
	public Parameter(@JsonProperty("name") final String name, @JsonProperty("defaultDomain") final IParameterDomain defaultDomain,@JsonProperty("defaultValue") final Object defaultValue) {
		super();
		this.name = name;
		this.defaultDomain = defaultDomain;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return this.name;
	}

	public IParameterDomain getDefaultDomain() {
		return this.defaultDomain;
	}

	public Object getDefaultValue() {
		return this.defaultValue;
	}

	public boolean isNumeric() {
		return this.defaultDomain instanceof NumericParameterDomain;
	}

	public boolean isCategorical() {
		return this.defaultDomain instanceof CategoricalParameterDomain;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.defaultDomain == null) ? 0 : this.defaultDomain.hashCode());
		result = prime * result + ((this.defaultValue == null) ? 0 : this.defaultValue.hashCode());
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
		Parameter other = (Parameter) obj;
		if (this.defaultDomain == null) {
			if (other.defaultDomain != null) {
				return false;
			}
		} else if (!this.defaultDomain.equals(other.defaultDomain)) {
			return false;
		}
		if (this.defaultValue == null) {
			if (other.defaultValue != null) {
				return false;
			}
		} else if (!this.defaultValue.equals(other.defaultValue)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
