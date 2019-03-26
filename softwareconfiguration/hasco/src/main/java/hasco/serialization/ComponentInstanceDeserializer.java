package hasco.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import hasco.model.Component;
import hasco.model.ComponentInstance;

public class ComponentInstanceDeserializer extends StdDeserializer<ComponentInstance> {

	/**
	 *
	 */
	private static final long serialVersionUID = 4216559441244072999L;

	public ComponentInstanceDeserializer() {
		super(ComponentInstance.class);
	}

	@SuppressWarnings("unchecked")
	public ComponentInstance readAsTree(final TreeNode p) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// read the parameter values
		Map<String, String> parameterValues = mapper.treeToValue(p.get("parameterValues"), HashMap.class);
		// read the component

		List<Object> componentList = new ArrayList<>();
		componentList.add(p.get("component"));

		ComponentLoader loader = new ComponentLoader();
		ObjectNode node = new ObjectMapper().createObjectNode();
		node.put("repository", "repository");
		node.set("components", new ObjectMapper().valueToTree(componentList));

		loader.readFromString(node.toString());

		Collection<Component> components = loader.getComponents();
		Component component = null;
		if (!components.isEmpty()) {
			component = components.iterator().next();
		}

		Map<String, ComponentInstance> satisfactionOfRequiredInterfaces = new HashMap<>();
		// recursively resolve the requiredInterfaces
		TreeNode n = p.get("satisfactionOfRequiredInterfaces");
		Iterator<String> fields = n.fieldNames();
		while (fields.hasNext()) {
			String key = fields.next();
			satisfactionOfRequiredInterfaces.put(key, this.readAsTree(n.get(key)));
		}
		return new ComponentInstance(component, parameterValues, satisfactionOfRequiredInterfaces);
	}

	@Override
	public ComponentInstance deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
		return this.readAsTree(p.readValueAsTree());
	}

}
