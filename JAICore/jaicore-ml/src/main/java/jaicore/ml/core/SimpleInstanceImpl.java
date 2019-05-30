package jaicore.ml.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jaicore.logging.LoggerUtil;
import jaicore.ml.interfaces.Instance;

@SuppressWarnings("serial")
public class SimpleInstanceImpl extends ArrayList<Double> implements Instance {

	private static final Logger logger = LoggerFactory.getLogger(SimpleInstanceImpl.class);

	public SimpleInstanceImpl() {

	}

	public SimpleInstanceImpl(final int initialCapacity) {
		super(initialCapacity);
	}

	public SimpleInstanceImpl(final Collection<Double> values) {
		super(values);
	}

	public SimpleInstanceImpl(final double[] values) {
		super(values.length);
		for (int i = 0; i < values.length; i++) {
			super.add(i, values[i]);
		}
	}

	public SimpleInstanceImpl(final String json) throws IOException {
		this(new ObjectMapper().readTree(json));
	}

	public SimpleInstanceImpl(final JsonNode jsonNode) {
		this();
		for (JsonNode val : jsonNode) {
			this.add(val.asDouble());
		}
	}

	@Override
	public String toJson() {
		ObjectMapper om = new ObjectMapper();
		try {
			return om.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			logger.error(LoggerUtil.getExceptionInfo(e));
			return null;
		}
	}

	@Override
	public int getNumberOfColumns() {
		return this.size();
	}
}
