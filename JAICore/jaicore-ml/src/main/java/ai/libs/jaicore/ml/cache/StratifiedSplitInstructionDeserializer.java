package ai.libs.jaicore.ml.cache;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;

public class StratifiedSplitInstructionDeserializer extends StdDeserializer<StratifiedSplitSubsetInstruction> {

	public StratifiedSplitInstructionDeserializer() {
		this(null);
	}

	public StratifiedSplitInstructionDeserializer(final Class<?> vc) {
		super(vc);
	}

	@Override
	public StratifiedSplitSubsetInstruction deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		int seed = (Integer) ((IntNode) node.get("seed")).numberValue();
		int ratio = (Integer) ((IntNode) node.get("")).numberValue();

		return new StratifiedSplitSubsetInstruction(seed, ratio);
	}

}
