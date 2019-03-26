package jaicore.ml.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

	private final Set<String> occurringLabels = new HashSet<>();

	public SimpleLabeledInstancesImpl() {
	}

	public SimpleLabeledInstancesImpl(final String json) throws IOException {
		this.addAllFromJson(json);
	}

	public SimpleLabeledInstancesImpl(final JsonNode jsonNode) {
		this.addAllFromJson(jsonNode);
	}

	public SimpleLabeledInstancesImpl(final File jsonFile) throws IOException {
		this.addAllFromJson(jsonFile);
	}

	@Override
	public boolean add(final LabeledInstance<String> instance) {
		/* check instance format */
		if (this.numColumns < 0) {
			this.numColumns = instance.getNumberOfColumns();
		} else if (this.numColumns != instance.getNumberOfColumns()) {
			throw new IllegalArgumentException("Cannot add " + instance.getNumberOfColumns() + "-valued instance to dataset with " + this.numColumns + " instances.");
		}

		this.occurringLabels.add(instance.getLabel());

		return super.add(instance);
	}

	@Override
	public int getNumberOfRows() {
		return this.size();
	}

	@Override
	public int getNumberOfColumns() {
		return this.numColumns;
	}

	@Override
	public String toJson() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode root = om.createObjectNode();
		ArrayNode instances = root.putArray("instances");
		ArrayNode labels = root.putArray("labels");
		for (LabeledInstance<String> instance : this) {
			ArrayNode instanceArray = instances.addArray();
			for (Double val : instance) {
				instanceArray.add(val);
			}
			labels.add(instance.getLabel());
		}
		try {
			return om.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ArrayList<String> getOccurringLabels() {
		return new ArrayList<>(this.occurringLabels);
	}

	@Override
	public void addAllFromJson(final String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(json);
		this.addAllFromJson(root);
	}

	public void addAllFromJson(final JsonNode jsonNode) {
		JsonNode instances = jsonNode.get("instances");
		JsonNode labels = jsonNode.get("labels");
		if (labels == null) {
			throw new IllegalArgumentException("No labels provided in the dataset!");
		}
		if (instances.size() != labels.size()) {
			throw new IllegalArgumentException("Number of labels does not match the number of instances!");
		}
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
	public void addAllFromJson(final File jsonFile) throws IOException {
		this.addAllFromJson(FileUtil.readFileAsString(jsonFile));
	}
}
