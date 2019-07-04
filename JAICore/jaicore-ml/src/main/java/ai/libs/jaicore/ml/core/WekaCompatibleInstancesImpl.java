package ai.libs.jaicore.ml.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.interfaces.LabeledInstance;

@SuppressWarnings("serial")
public class WekaCompatibleInstancesImpl extends SimpleLabeledInstancesImpl {

	private static final String DECLARED_CLASSES = "declaredclasses";
	private final transient Logger logger = LoggerFactory.getLogger(WekaCompatibleInstancesImpl.class);
	private final List<String> declaredClasses;

	public WekaCompatibleInstancesImpl(final List<String> declaredClasses) {
		this.declaredClasses = new ArrayList<>(declaredClasses);
	}

	public WekaCompatibleInstancesImpl(final String json) throws IOException {
		this (new ObjectMapper().readTree(json));
	}

	public WekaCompatibleInstancesImpl(final JsonNode jsonNode) {
		if (!jsonNode.has(DECLARED_CLASSES)) {
			throw new IllegalArgumentException("Given JSON serialization does not specify the declared classes, which is required for WEKA compatibility.");
		}
		JsonNode declaredClassesNode = jsonNode.get(DECLARED_CLASSES);
		if (!declaredClassesNode.isArray()) {
			throw new IllegalArgumentException("Class declaration in given JSON is not an array, which is required for WEKA compatibility.");
		}
		this.declaredClasses = new ArrayList<>();
		for (JsonNode c : jsonNode.get(DECLARED_CLASSES)) {
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
			ArrayNode declaredClassesNode = node.putArray(DECLARED_CLASSES);
			for (String declaredClass : this.declaredClasses) {
				declaredClassesNode.add(declaredClass);
			}
			return node.toString();
		} catch (IOException e) {
			this.logger.error(LoggerUtil.getExceptionInfo(e));
		}
		throw new IllegalStateException("Could not convert dataset to json");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.declaredClasses == null) ? 0 : this.declaredClasses.hashCode());
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
		WekaCompatibleInstancesImpl other = (WekaCompatibleInstancesImpl) obj;
		if (this.declaredClasses == null) {
			if (other.declaredClasses != null) {
				return false;
			}
		} else if (!this.declaredClasses.equals(other.declaredClasses)) {
			return false;
		}
		return true;
	}
}
