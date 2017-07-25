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
	public void addAllFromJson(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode instances = mapper.readTree(jsonString);
			for (JsonNode instanceAsJson : instances) {
				Instance instance = new SimpleInstanceImpl();
				for (JsonNode val : instanceAsJson) {
					instance.add(val.asDouble());
				}
				this.add(instance);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addAllFromJson(File jsonFile) throws IOException {
		addAllFromJson(FileUtil.readFileAsString(jsonFile));
	}

}
