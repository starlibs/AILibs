package hasco.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name", "defaultDomain", "defaultValue"})
public class Parameter {
	private final String name;
	private final IParameterDomain defaultDomain;
	private final Object defaultValue;

	
	public Parameter(@JsonProperty("name") String name, @JsonProperty("defaultDomain")IParameterDomain defaultDomain,@JsonProperty("defaultValue") Object defaultValue) {
		super();
		this.name = name;
		this.defaultDomain = defaultDomain;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public IParameterDomain getDefaultDomain() {
		return defaultDomain;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public boolean isNumeric() {
		return defaultDomain instanceof NumericParameterDomain;
	}
	
	public boolean isCategorical() {
		return defaultDomain instanceof CategoricalParameterDomain;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultDomain == null) ? 0 : defaultDomain.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Parameter other = (Parameter) obj;
		if (defaultDomain == null) {
			if (other.defaultDomain != null)
				return false;
		} else if (!defaultDomain.equals(other.defaultDomain))
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}
