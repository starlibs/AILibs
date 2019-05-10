package jaicore.ml.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jaicore.basic.FileUtil;
import jaicore.ml.interfaces.LabeledInstance;

@SuppressWarnings("serial")
public class WekaCompatibleInstancesImpl extends SimpleLabeledInstancesImpl {

	private final List<String> declaredClasses;

	public WekaCompatibleInstancesImpl(final List<String> declaredClasses) {
		this.declaredClasses = new ArrayList<>(declaredClasses);
	}

	public WekaCompatibleInstancesImpl(final String json) throws IOException {
		this (new ObjectMapper().readTree(json));
	}

	public WekaCompatibleInstancesImpl(final JsonNode jsonNode) {
		if (!jsonNode.has("declaredclasses")) {
			throw new IllegalArgumentException("Given JSON serialization does not specify the declared classes, which is required for WEKA compatibility.");
		}
		JsonNode declaredClasses = jsonNode.get("declaredclasses");
		if (!declaredClasses.isArray()) {
			throw new IllegalArgumentException("Class declaration in given JSON is not an array, which is required for WEKA compatibility.");
		}
		this.declaredClasses = new ArrayList<>();
		for (JsonNode c : jsonNode.get("declaredclasses")) {
			this.declaredClasses.add(c.asText());
		}
		this.addAllFromJson(jsonNode);
	}

	public WekaCompatibleInstancesImpl(final File jsonFile) throws IOException {
		this (FileUtil.readFileAsString(jsonFile));
	}

	@Override
	public boolean add(final LabeledInstance<String> i) {
		if (!this.declaredClasses.contains(i.getLabel())) {
			throw new IllegalArgumentException("Instance with label " + i.getLabel() + " cannot be inserted in a dataset with declared labels " + this.declaredClasses);
		}
		return super.add(i);
	}

	public List<String> getDeclaredClasses() {
		return Collections.unmodifiableList(this.declaredClasses);
	}

	@Override
	public String toJson() {
		String json = super.toJson();
		try {
			ObjectNode node = (ObjectNode)new ObjectMapper().readTree(json);
			ArrayNode declaredClasses = node.putArray("declaredclasses");
			for (String declaredClass : this.declaredClasses) {
				declaredClasses.add(declaredClass);
			}
			return node.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new IllegalStateException("Could not convert dataset to json");
	}
}
