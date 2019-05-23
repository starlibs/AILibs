package hasco.serialization;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

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

	private transient Collection<Component> possibleComponents; // the idea is not to serialize the deserializer, so this can be transient

	public ComponentInstanceDeserializer(Collection<Component> possibleComponents) {
		super(ComponentInstance.class);
		this.possibleComponents = possibleComponents;
	}

	@SuppressWarnings("unchecked")
	public ComponentInstance readAsTree(final TreeNode p) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// read the parameter values
		Map<String, String> parameterValues = mapper.treeToValue(p.get("parameterValues"), HashMap.class);
		// read the component

		String componentName = p.get("component").get("name").toString().replaceAll("\"", "");

		Component component = possibleComponents.stream().filter(c -> c.getName().equals(componentName)).findFirst()
				.orElseThrow(NoSuchElementException::new);

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
