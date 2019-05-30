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

/**
 * This class provides a util for serializing specific contents of an object in the form of a JSON string. Furthermore, it recursively extracts json trees from object variables and mounts them into the current tree.
 *
 * @author wever
 */
public class ToJSONStringUtil {

	/* Prevent this class to be instantiated. */
	private ToJSONStringUtil() {
		// intentionally left blank.
	}

	/**
	 * Util for transforming an object into a JSON string representation. It requires to pass a map of object variables of the object to serialize in form of a map.
	 *
	 * @param fields The map of object variables to be serialized as a json string.
	 * @return A string representing the object contents in JSON format.
	 */
	public static String toJSONString(final Map<String, Object> fields) {
		ObjectMapper om = new ObjectMapper();
		ObjectNode root = om.createObjectNode();

		for (Entry<String, Object> field : fields.entrySet()) {
			root.set(field.getKey(), parseObjectToJsonNode(field.getValue(), om));
		}

		try {
			return om.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			return "JSON representation of toString could not be generated.";
		}
	}

	/**
	 * This method should be used with caution, as it introduces an additional layer for naming the object to serialize in json format with <code>rootName</code>.
	 * Thus, it introduces another layer in the object hierarchy.
	 *
	 * @param rootName The name of the object.
	 * @param fields The map of object variables to be serialized as a json string.
	 * @return A string representing the object contents in JSON format.
	 */
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

	/**
	 * Parses an object to JSON depending on the objects capabilities to express itself in JSON format or not.
	 *
	 * @param fieldValue The object to be parsed.
	 * @param om The object mapper that is to be used for parsing.
	 * @return A JsonNode representing the parsed object.
	 */
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
