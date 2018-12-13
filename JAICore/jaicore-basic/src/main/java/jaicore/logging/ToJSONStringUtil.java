package jaicore.logging;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class ToJSONStringUtil {

	private ToJSONStringUtil() {
		// intentionally left blank.
	}

	public static String toJSONString(final Map<String, Object> fields) {
		String callersName = Thread.currentThread().getStackTrace()[1].getClass().getSimpleName();
		return toJSONString(callersName, fields);
	}

	public static String toJSONString(final String rootName, final Map<String, Object> fields) {
		ObjectMapper om = new ObjectMapper();
		ObjectNode root = om.createObjectNode();
		ObjectNode innerObject = om.createObjectNode();

		root.set(rootName, innerObject);

		for (Entry<String, Object> field : fields.entrySet()) {
			innerObject.set(field.getKey(), parseObjectToJsonNode(field.getValue(), om));
		}

		try {
			return om.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return rootName + " JSON representation of toString could not be generated.";
		}
	}

	@SuppressWarnings("unchecked")
	public static JsonNode parseObjectToJsonNode(final Object fieldValue, final ObjectMapper om) {
		if (fieldValue == null) {
			return new TextNode(fieldValue + "");
		}
		if (fieldValue instanceof JsonNode) {
			return (JsonNode) fieldValue;
		}

		if (fieldValue instanceof Collection<?>) {
			ArrayNode valueArray = om.createArrayNode();
			for (Object value : (Collection<?>) fieldValue) {
				valueArray.add(parseObjectToJsonNode(value, om));
			}
			return valueArray;
		} else if (fieldValue.getClass().isArray()) {
			ArrayNode valueArray = om.createArrayNode();
			for (Object value : (Object[]) fieldValue) {
				valueArray.add(parseObjectToJsonNode(value, om));
			}
			return valueArray;
		} else if (fieldValue instanceof Map) {
			ObjectNode valueMap = om.createObjectNode();
			for (Entry<Object, Object> entry : ((Map<Object, Object>) fieldValue).entrySet()) {
				valueMap.set(entry.getKey() + "", parseObjectToJsonNode(entry.getValue(), om));
			}
			return valueMap;
		}

		try {
			return om.readTree(fieldValue + "");
		} catch (Exception e) {
			return new TextNode(fieldValue + "");
		}
	}

}
