package ai.libs.jaicore.components.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;

public class ComponentInstanceDeserializer extends StdDeserializer<ComponentInstance> {

	/**
	 *
	 */
	private static final long serialVersionUID = 4216559441244072999L;

	private transient Collection<IComponent> possibleComponents; // the idea is not to serialize the deserializer, so this can be transient

	public ComponentInstanceDeserializer(final Collection<? extends IComponent> possibleComponents) {
		super(ComponentInstance.class);
		this.possibleComponents = new ArrayList<>(possibleComponents);
	}

	public ComponentInstance readFromJson(final String json) throws IOException {
		return this.readAsTree(new ObjectMapper().readTree(json));
	}

	@SuppressWarnings("unchecked")
	public ComponentInstance readAsTree(final JsonNode p) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// read the parameter values
		Map<String, String> parameterValues = new HashMap<>();
		if (p.has("params")) {
			parameterValues = mapper.treeToValue(p.get("params"), HashMap.class);
		}
		// read the component

		String componentName = p.get("component").toString().replace("\"", "");

		IComponent component = this.possibleComponents.stream().filter(c -> c.getName().equals(componentName)).findFirst().orElseThrow(NoSuchElementException::new);

		Map<String, List<IComponentInstance>> satisfactionOfRequiredInterfaces = new HashMap<>();
		// recursively resolve the requiredInterfaces
		JsonNode n = p.get("requiredInterfaces");
		if (n != null) {
			Iterator<String> fields = n.fieldNames();
			while (fields.hasNext()) {
				String key = fields.next();
				ArrayNode provisions = (ArrayNode)n.get(key);
				List<IComponentInstance> componentInstances = new ArrayList<>();
				for (JsonNode ci : provisions) {
					componentInstances.add(this.readAsTree(ci));
				}
				satisfactionOfRequiredInterfaces.put(key, componentInstances);
			}
		}
		return new ComponentInstance(component, parameterValues, satisfactionOfRequiredInterfaces);
	}

	@Override
	public ComponentInstance deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
		return this.readAsTree(p.readValueAsTree());
	}

}
