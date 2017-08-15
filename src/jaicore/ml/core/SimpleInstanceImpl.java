package jaicore.ml.core;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jaicore.ml.interfaces.Instance;

@SuppressWarnings("serial")
public class SimpleInstanceImpl extends ArrayList<Double> implements Instance {

	public SimpleInstanceImpl() {

	}

	public SimpleInstanceImpl(String json) throws IOException {
		this(new ObjectMapper().readTree(json));
	}

	public SimpleInstanceImpl(JsonNode jsonNode) {
		this();
		for (JsonNode val : jsonNode) {
			add(val.asDouble());
		}
	}

	@Override
	public String toJson() {
		ObjectMapper om = new ObjectMapper();
		try {
			return om.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getNumberOfColumns() {
		return this.size();
	}
}
