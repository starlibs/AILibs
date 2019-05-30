package hasco.serialization;

import java.io.IOException;
import java.util.LinkedList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import hasco.model.CategoricalParameterDomain;
import hasco.model.IParameterDomain;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;

public class ParameterDeserializer extends StdDeserializer<Parameter> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ParameterDeserializer() {
		this(null);
	}

	public ParameterDeserializer(final Class<Parameter> vc) {
		super(vc);
	}

	@Override
	public Parameter deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
		JsonNode node = jp.getCodec().readTree(jp);
		String name = node.get("name").asText();
		boolean numeric = node.get("numeric").asBoolean();
		boolean categorical = node.get("categorical").asBoolean();
		boolean defaultValue = node.get("defaultValue").asBoolean();
		IParameterDomain domain = null;
		JsonNode domainNode = node.get("defaultDomain");
		if (numeric) {
			boolean isInteger = domainNode.get("integer").asBoolean();
			double min = domainNode.get("min").asDouble();
			double max = domainNode.get("max").asDouble();
			domain = new NumericParameterDomain(isInteger, min, max);
		} else if (categorical) {
			LinkedList<String> values = new LinkedList<>();
			JsonNode arrayNode = domainNode.get("values");
			if (arrayNode.isArray()) {
				for (final JsonNode valueNode : arrayNode) {
					values.add(valueNode.asText());
				}
			}
			domain = new CategoricalParameterDomain(values);
		}

		return new Parameter(name, domain, defaultValue);
	}

}
