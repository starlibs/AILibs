package jaicore.ml.core;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jaicore.logging.LoggerUtil;
import jaicore.ml.interfaces.LabeledInstance;

@SuppressWarnings("serial")
public class SimpleLabeledInstanceImpl extends ArrayList<Double> implements LabeledInstance<String> {

	private static final String K_ATTRIBUTES = "attributes";
	private static final String K_LABEL = "label";

	private String label;
	private static final Logger logger = LoggerFactory.getLogger(SimpleLabeledInstanceImpl.class);

	public SimpleLabeledInstanceImpl() {
		super();
	}

	public SimpleLabeledInstanceImpl(final String json) throws IOException {
		this(new ObjectMapper().readTree(json));
	}

	public SimpleLabeledInstanceImpl(final JsonNode jsonNode) {
		this();
		if (!jsonNode.has(K_ATTRIBUTES)) {
			throw new IllegalArgumentException("JSON representation has no attribute \"attributes\".");
		}
		if (!jsonNode.has(K_LABEL)) {
			throw new IllegalArgumentException("JSON representation has no attribute \"label\".");
		}
		for (JsonNode val : jsonNode.get(K_ATTRIBUTES)) {
			this.add(val.asDouble());
		}
		this.label = jsonNode.get(K_LABEL).asText();
	}

	@Override
	public String toJson() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode root = om.createObjectNode();
		ArrayNode attributes = root.putArray(K_ATTRIBUTES);
		root.put(K_LABEL, this.label);
		for (double d : this) {
			attributes.add(d);
		}
		try {
			return om.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			logger.error(LoggerUtil.getExceptionInfo(e));
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
		result = prime * result + ((this.label == null) ? 0 : this.label.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SimpleLabeledInstanceImpl other = (SimpleLabeledInstanceImpl) obj;
		if (this.label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!this.label.equals(other.label)) {
			return false;
		}
		return true;
	}
}
