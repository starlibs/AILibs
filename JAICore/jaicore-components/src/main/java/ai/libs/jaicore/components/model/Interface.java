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

	public Integer getMin() {
		return this.min;
	}

	public Integer getMax() {
		return this.max;
	}
}
