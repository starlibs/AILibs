package hasco.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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

	public ComponentInstance readAsTree(TreeNode p) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// read the parameter values
		Map<String, String> parameterValues = mapper.treeToValue(p.get("parameterValues"), HashMap.class);
		// read the component

		List<Object> componentList = new ArrayList();
		componentList.add(p.get("component"));

		ComponentLoader loader = new ComponentLoader();
		JSONObject node = new JSONObject();
		node.put("repository", "repository");
		node.put("components", componentList);
		
		loader.readFromString(node.toJSONString());

		Collection<Component> components = loader.getComponents();
		Component component = null;
		if (!components.isEmpty())
			component = components.iterator().next();
		
		Map<String, ComponentInstance> satisfactionOfRequiredInterfaces = new HashMap<>();
		// recursively resolve the requiredInterfaces
		TreeNode n = p.get("satisfactionOfRequiredInterfaces");
		Iterator<String> fields = n.fieldNames();
		while (fields.hasNext()) {
			String key = fields.next();
			satisfactionOfRequiredInterfaces.put(key, readAsTree(n.get(key)));
		}
		return new ComponentInstance(component, parameterValues, satisfactionOfRequiredInterfaces);
	}

	@Override
	public ComponentInstance deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		return readAsTree(p.readValueAsTree());
	}

}
