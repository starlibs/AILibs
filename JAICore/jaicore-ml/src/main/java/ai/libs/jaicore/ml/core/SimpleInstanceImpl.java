package ai.libs.jaicore.ml.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.ml.interfaces.Instance;

@SuppressWarnings("serial")
public class SimpleInstanceImpl extends ArrayList<Double> implements Instance {

	public SimpleInstanceImpl() {

	}
	
	public SimpleInstanceImpl(int initialCapacity) {
		super(initialCapacity);
	}
	
	public SimpleInstanceImpl(Collection<Double> values) {
		super(values);
	}
	
	public SimpleInstanceImpl(double[] values) {
		super(values.length);
		for (int i = 0; i < values.length; i++)
			super.add(i, values[i]);
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
