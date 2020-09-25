package ai.libs.jaicore.components.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;

@JsonPropertyOrder({ "id", "name", "optional", "min", "max" })
public class Interface implements IRequiredInterfaceDefinition, Serializable {
	private static final long serialVersionUID = -668580435561427897L;
	private final String id;
	private final String name;
	private final boolean optional;
	private final boolean uniqueComponents;
	private final boolean ordered;
	private final Integer min;
	private final Integer max;

	@JsonCreator
	public Interface(@JsonProperty("id") final String id, @JsonProperty("name") final String name, @JsonProperty("optional") final Boolean optional, @JsonProperty("uniquecomponents") final Boolean uniqueComponents, @JsonProperty("ordered") final Boolean ordered, @JsonProperty("min") final Integer min, @JsonProperty("max") final Integer max) {
		this.id = id;
		this.name = name;
		this.optional = optional;
		this.ordered = ordered;
		this.uniqueComponents = uniqueComponents;
		this.min = min;
		this.max = max;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getMin() {
		return this.min;
	}

	@Override
	public int getMax() {
		return this.max;
	}

	@Override
	public boolean isUniqueComponents() {
		return this.uniqueComponents;
	}

	@Override
	public boolean isOrdered() {
		return this.ordered;
	}

	@Override
	public boolean isOptional() {
		return this.optional;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.max == null) ? 0 : this.max.hashCode());
		result = prime * result + ((this.min == null) ? 0 : this.min.hashCode());
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + (this.optional ? 1231 : 1237);
		result = prime * result + (this.ordered ? 1231 : 1237);
		result = prime * result + (this.uniqueComponents ? 1231 : 1237);
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
		Interface other = (Interface) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		if (this.max == null) {
			if (other.max != null) {
				return false;
			}
		} else if (!this.max.equals(other.max)) {
			return false;
		}
		if (this.min == null) {
			if (other.min != null) {
				return false;
			}
		} else if (!this.min.equals(other.min)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.optional != other.optional) {
			return false;
		}
		if (this.ordered != other.ordered) {
			return false;
		}
		return this.uniqueComponents == other.uniqueComponents;
	}
}
