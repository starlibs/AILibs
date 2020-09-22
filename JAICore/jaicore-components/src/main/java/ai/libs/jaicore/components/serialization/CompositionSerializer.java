package ai.libs.jaicore.components.serialization;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.components.api.IComponentInstance;

public class CompositionSerializer {

	private CompositionSerializer() {
		/* avoids instantiation */
	}

	public static ObjectNode serializeComponentInstance(final IComponentInstance instance) {
		Objects.requireNonNull(instance);
		ObjectMapper om = new ObjectMapper();
		ObjectNode on = om.createObjectNode();

		/* define component and params */
		on.put("component", instance.getComponent().getName());
		ObjectNode params = om.createObjectNode();
		for (String paramName : instance.getParameterValues().keySet()) {
			params.put(paramName, instance.getParameterValues().get(paramName));
		}
		on.set("params", params);

		/* define how required interfaces have been resolved */
		ObjectNode requiredInterfaces = om.createObjectNode();
		for (String requiredInterface : instance.getSatisfactionOfRequiredInterfaces().keySet()) {
			ArrayNode componentInstancesHere = om.createArrayNode();
			instance.getSatisfactionOfRequiredInterfaces().get(requiredInterface).forEach(ci -> componentInstancesHere.add(serializeComponentInstance(ci)));
			requiredInterfaces.set(requiredInterface, componentInstancesHere);
		}
		on.set("requiredInterfaces", requiredInterfaces);

		return on;
	}
}
