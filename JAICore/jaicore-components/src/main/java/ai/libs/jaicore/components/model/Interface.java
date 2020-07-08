package ai.libs.jaicore.components.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "name", "optional", "min", "max" })
public class Interface implements Serializable {
	private static final long serialVersionUID = -668580435561427897L;
	private final String id;
	private final String name;
	private final Integer min;
	private final Integer max;

	@JsonCreator
	public Interface(@JsonProperty("id") final String id, @JsonProperty("name") final String name, @JsonProperty("optional") final Boolean optional, @JsonProperty("min") final Integer min, @JsonProperty("max") final Integer max) {
		this.id = id;
		this.name = name;

		if (optional == null && (min != null && max != null)) {
			this.min = min;
			this.max = max;
		} else if (optional != null && (min == null && max == null)) {
			this.min = 0;
			this.max = 1;
		} else {
			this.min = 1;
			this.max = 1;
		}
	}

	public Interface(final String id, final String name, final Integer min, final Integer max) {
		this.id = id;
		this.name = name;
		this.min = min;
		this.max = max;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.max == null) ? 0 : this.max.hashCode());
		result = prime * result + ((this.min == null) ? 0 : this.min.hashCode());
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
		return true;
	}
}
