package jaicore.graphvisualizer.events.recorder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventHistory;

public class AlgorithmEventHistorySerializer {

	private ObjectMapper objectMapper;

	public AlgorithmEventHistorySerializer() {
		initializeObjectMapper();
	}

	private void initializeObjectMapper() {
		objectMapper = new ObjectMapper();

		// make sure that the object mapper sees all fields
		objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		// make sure that the object mapper stores type information when serializing objects
		objectMapper.enableDefaultTyping();
	}

	public String serializeAlgorithmEventHistory(AlgorithmEventHistory algorithmEventHistory) throws JsonProcessingException {

		List<AlgorithmEventHistoryEntry> algorithmEventHistoryEntries = new LinkedList<>();
		for (int i = 0; i < algorithmEventHistory.getLength(); i++) {
			AlgorithmEventHistoryEntry entry = algorithmEventHistory.getEntryAtTimeStep(i);
			algorithmEventHistoryEntries.add(entry);
		}
		PropertyProcessedAlgorithmEventHistory serializableAlgorithmEventHistory = new PropertyProcessedAlgorithmEventHistory(algorithmEventHistoryEntries);

		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serializableAlgorithmEventHistory);
	}

	public AlgorithmEventHistory deserializeAlgorithmEventHistory(String serializedAlgorithmEventHistory) throws JsonParseException, JsonMappingException, IOException {
		PropertyProcessedAlgorithmEventHistory serializableAlgorithmEventHistory = objectMapper.readValue(serializedAlgorithmEventHistory, PropertyProcessedAlgorithmEventHistory.class);
		AlgorithmEventHistory algorithmEventHistory = new AlgorithmEventHistory(serializableAlgorithmEventHistory.getEntries());

		return algorithmEventHistory;
	}

}
