package jaicore.ml.core;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jaicore.ml.interfaces.LabeledInstance;

@SuppressWarnings("serial")
public class SimpleLabeledInstanceImpl extends ArrayList<Double> implements LabeledInstance<String> {

	private String label;

	public SimpleLabeledInstanceImpl() {
		super();
	}
	
	public SimpleLabeledInstanceImpl(String json) throws IOException {
		this(new ObjectMapper().readTree(json));
	}

	public SimpleLabeledInstanceImpl(JsonNode jsonNode) {
		this();
		if (!jsonNode.has("attributes"))
			throw new IllegalArgumentException("JSON representation has no attribute \"attributes\".");
		if (!jsonNode.has("label"))
			throw new IllegalArgumentException("JSON representation has no attribute \"label\".");
		for (JsonNode val : jsonNode.get("attributes")) {
			add(val.asDouble());
		}
		this.label = jsonNode.get("label").asText();
	}

	@Override
	public String toJson() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode root = om.createObjectNode();
		ArrayNode attributes = root.putArray("attributes");
		root.put("label", this.label);
		for (double d : this)
			attributes.add(d);
		try {
			return om.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getNumberOfColumns() {
		return this.size();
	}

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String toString() {
		return "{data=" + super.toString() + ", label=" + this.label + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleLabeledInstanceImpl other = (SimpleLabeledInstanceImpl) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
}
