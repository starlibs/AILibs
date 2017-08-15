package jaicore.ml.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jaicore.basic.FileUtil;
import jaicore.ml.interfaces.LabeledInstance;
import jaicore.ml.interfaces.LabeledInstances;

@SuppressWarnings("serial")
public class SimpleLabeledInstancesImpl extends ArrayList<LabeledInstance<String>> implements LabeledInstances<String> {

	private int numColumns = -1;
	
	private Set<String> occurringLabels = new HashSet<>();
	
	public SimpleLabeledInstancesImpl() { }
	
	public SimpleLabeledInstancesImpl(String json) throws IOException { addAllFromJson(json); }
	
	public SimpleLabeledInstancesImpl(JsonNode jsonNode) { addAllFromJson(jsonNode); }
	
	public SimpleLabeledInstancesImpl(File jsonFile) throws IOException { addAllFromJson(jsonFile); }

	@Override
	public boolean add(LabeledInstance<String> instance) {

		/* check instance format */
		if (numColumns < 0)
			numColumns = instance.getNumberOfColumns();
		else if (numColumns != instance.getNumberOfColumns())
			throw new IllegalArgumentException("Cannot add " + instance.getNumberOfColumns() + "-valued instance to dataset with " + numColumns + " instances.");

		occurringLabels.add(instance.getLabel());

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
		ObjectNode root = om.createObjectNode();
		ArrayNode instances = root.putArray("instances");
		ArrayNode labels = root.putArray("labels");
		for (LabeledInstance<String> instance : this) {
			ArrayNode instanceArray = instances.addArray();
			for (Double val : instance)
				instanceArray.add(val);
			labels.add(instance.getLabel().toString());
		}
		try {
			return om.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Collection<String> getOccurringLabels() {
		return new ArrayList<>(occurringLabels);
	}
	

	@Override
	public void addAllFromJson(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(json);
		addAllFromJson(root);
	}

	public void addAllFromJson(JsonNode jsonNode) {
		JsonNode instances = jsonNode.get("instances");
		JsonNode labels = jsonNode.get("labels");
		if (labels == null)
			throw new IllegalArgumentException("No labels provided in the dataset!");
		if (instances.size() != labels.size())
			throw new IllegalArgumentException("Number of labels does not match the number of instances!");
		int index = 0;
		for (JsonNode instance : instances) {
			LabeledInstance<String> labeledInstance = new SimpleLabeledInstanceImpl();
			for (JsonNode val : instance) {
				labeledInstance.add(val.asDouble());
			}
			labeledInstance.setLabel(labels.get(index++).asText());
			this.add(labeledInstance);
		}
	}

	@Override
	public void addAllFromJson(File jsonFile) throws IOException {
		addAllFromJson(FileUtil.readFileAsString(jsonFile));
	}
}
