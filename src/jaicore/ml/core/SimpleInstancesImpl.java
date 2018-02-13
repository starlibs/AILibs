package jaicore.ml.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jaicore.basic.FileUtil;
import jaicore.ml.interfaces.Instance;
import jaicore.ml.interfaces.Instances;

@SuppressWarnings("serial")
public class SimpleInstancesImpl extends ArrayList<Instance> implements Instances {

	private int numColumns = -1;

	public SimpleInstancesImpl() {
	}
	
	public SimpleInstancesImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public SimpleInstancesImpl(String json) throws IOException {
		addAllFromJson(json);
	}

	public SimpleInstancesImpl(JsonNode jsonNode) {
		addAllFromJson(jsonNode);
	}

	public SimpleInstancesImpl(File jsonFile) throws IOException {
		addAllFromJson(jsonFile);
	}
	
	public boolean add(double[] values) {
		return super.add(new SimpleInstanceImpl(values));
	}

	@Override
	public boolean add(Instance instance) {

		/* check instance format */
		if (numColumns < 0)
			numColumns = instance.getNumberOfColumns();
		else if (numColumns != instance.getNumberOfColumns())
			throw new IllegalArgumentException("Cannot add " + instance.getNumberOfColumns() + "-valued instance to dataset with " + numColumns + " instances.");

		return super.add(instance);
	}

	@Override
	public int getNumberOfRows() {
		return this.size();
	}

	@Override
	public int getNumberOfColumns() {
		return numColumns;
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
	public void addAllFromJson(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(json);
		addAllFromJson(root);
	}

	public void addAllFromJson(JsonNode jsonNode) {
		if (!jsonNode.isArray())
			throw new IllegalArgumentException("Root node from parsed JSON tree is not an array!");
		for (JsonNode instanceAsJson : jsonNode) {
			Instance instance = new SimpleInstanceImpl(instanceAsJson);
			this.add(instance);
		}
	}

	@Override
	public void addAllFromJson(File jsonFile) throws IOException {
		addAllFromJson(FileUtil.readFileAsString(jsonFile));
	}

}
