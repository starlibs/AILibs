package ai.libs.jaicore.ml.core;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.interfaces.Instances;

@SuppressWarnings("serial")
public class SimpleInstancesImpl extends ASimpleInstancesImpl<SimpleInstanceImpl> implements Instances<SimpleInstanceImpl> {

	public SimpleInstancesImpl() {
	}

	public SimpleInstancesImpl(final int initialCapacity) {
		super(initialCapacity);
	}

	public SimpleInstancesImpl(final String json) throws IOException {
		this.addAllFromJson(json);
	}

	public SimpleInstancesImpl(final JsonNode jsonNode) {
		this.addAllFromJson(jsonNode);
	}

	public SimpleInstancesImpl(final File jsonFile) throws IOException {
		this.addAllFromJson(jsonFile);
	}

	public boolean add(final double[] values) {
		return super.add(new SimpleInstanceImpl(values));
	}

	@Override
	public boolean add(final SimpleInstanceImpl instance) {

		/* check instance format */
		if (this.numColumns < 0) {
			this.numColumns = instance.getNumberOfColumns();
		} else if (this.numColumns != instance.getNumberOfColumns()) {
			throw new IllegalArgumentException("Cannot add " + instance.getNumberOfColumns() + "-valued instance to dataset with " + this.numColumns + " instances.");
		}

		return super.add(instance);
	}

	@Override
	public String toJson() throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(this);
	}

	@Override
	public void addAllFromJson(final String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(json);
		this.addAllFromJson(root);
	}

	public void addAllFromJson(final JsonNode jsonNode) {
		if (!jsonNode.isArray()) {
			throw new IllegalArgumentException("Root node from parsed JSON tree is not an array!");
		}
		for (JsonNode instanceAsJson : jsonNode) {
			SimpleInstanceImpl instance = new SimpleInstanceImpl(instanceAsJson);
			this.add(instance);
		}
	}

	@Override
	public void addAllFromJson(final File jsonFile) throws IOException {
		this.addAllFromJson(FileUtil.readFileAsString(jsonFile));
	}

}
