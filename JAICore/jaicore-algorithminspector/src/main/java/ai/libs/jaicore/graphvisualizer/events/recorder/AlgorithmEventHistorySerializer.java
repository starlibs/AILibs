package ai.libs.jaicore.graphvisualizer.events.recorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventHistory;

/**
 * An {@link AlgorithmEventHistorySerializer} can be used to read and store {@link AlgorithmEventHistory}s in the form of JSON files.
 *
 * @author atornede
 *
 */
public class AlgorithmEventHistorySerializer {

	private ObjectMapper objectMapper;

	/**
	 * Creates a new {@link AlgorithmEventHistorySerializer}.
	 */
	public AlgorithmEventHistorySerializer() {
		this.initializeObjectMapper();
	}

	private void initializeObjectMapper() {
		this.objectMapper = new ObjectMapper();

		// make sure that the object mapper sees all fields
		this.objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		this.objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		// make sure that the object mapper stores type information when serializing objects
		this.objectMapper.enableDefaultTyping();
	}

	/**
	 * Serializes the given {@link AlgorithmEventHistory} into a JSON {@link String}.
	 *
	 * @param algorithmEventHistory The {@link AlgorithmEventHistory} to be serialized.
	 * @return The JSON {@link String} representing the serialized {@link AlgorithmEventHistory}.
	 * @throws JsonProcessingException If something went wrong during the transformation to JSON.
	 */
	public String serializeAlgorithmEventHistory(final AlgorithmEventHistory algorithmEventHistory) throws JsonProcessingException {

		List<AlgorithmEventHistoryEntry> algorithmEventHistoryEntries = new LinkedList<>();
		for (int i = 0; i < algorithmEventHistory.getLength(); i++) {
			AlgorithmEventHistoryEntry entry = algorithmEventHistory.getEntryAtTimeStep(i);
			algorithmEventHistoryEntries.add(entry);
		}
		PropertyProcessedAlgorithmEventHistory serializableAlgorithmEventHistory = new PropertyProcessedAlgorithmEventHistory(algorithmEventHistoryEntries);

		return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serializableAlgorithmEventHistory);
	}

	/**
	 * Deserializes the given JSON {@link String} into an {@link AlgorithmEventHistory} assuming it represents such an {@link AlgorithmEventHistory}.
	 *
	 * @param serializedAlgorithmEventHistory A JSON {@link String} representing an {@link AlgorithmEventHistory}.
	 * @return An {@link AlgorithmEventHistory} constructed from the given serialized algorithm event history.
	 * @throws JsonParseException If something went wrong during the transformation to JSON.
	 * @throws JsonMappingException If something went wrong during the transformation to JSON.
	 * @throws IOException If something went wrong during the transformation to JSON.
	 */
	public AlgorithmEventHistory deserializeAlgorithmEventHistory(final String serializedAlgorithmEventHistory) throws IOException {
		PropertyProcessedAlgorithmEventHistory serializableAlgorithmEventHistory = this.objectMapper.readValue(serializedAlgorithmEventHistory, PropertyProcessedAlgorithmEventHistory.class);

		return new AlgorithmEventHistory(serializableAlgorithmEventHistory.getEntries());
	}

	/**
	 * Deserializes the given JSON {@link File} into an {@link AlgorithmEventHistory} assuming it represents such an {@link AlgorithmEventHistory}.
	 *
	 * @param serializedAlgorithmEventHistory A JSON {@link String} representing an {@link AlgorithmEventHistory}.
	 * @return An {@link AlgorithmEventHistory} constructed from the given serialized algorithm event history.
	 * @throws JsonParseException If something went wrong during the transformation to JSON.
	 * @throws JsonMappingException If something went wrong during the transformation to JSON.
	 * @throws IOException If something went wrong during the transformation to JSON.
	 */
	public AlgorithmEventHistory deserializeAlgorithmEventHistory(final File serializedAlgorithmEventHistory) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(serializedAlgorithmEventHistory.toURI()));
		StringJoiner joiner = new StringJoiner(" ");
		for (String line : lines) {
			joiner.add(line);
		}
		return this.deserializeAlgorithmEventHistory(joiner.toString());
	}

}
