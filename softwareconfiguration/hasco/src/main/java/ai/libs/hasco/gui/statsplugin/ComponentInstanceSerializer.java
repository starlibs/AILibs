package ai.libs.hasco.gui.statsplugin;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.hasco.model.ComponentInstance;

public class ComponentInstanceSerializer {

	private ObjectMapper objectMapper;

	public ComponentInstanceSerializer() {
		initializeObjectMapper();
	}

	public String serializeComponentInstance(ComponentInstance componentInstance) throws JsonProcessingException {
		return objectMapper.writeValueAsString(componentInstance);
	}

	public ComponentInstance deserializeComponentInstance(String serializedComponentInstance) throws IOException {
		return objectMapper.readValue(serializedComponentInstance, ComponentInstance.class);
	}

	private void initializeObjectMapper() {
		objectMapper = new ObjectMapper();

		objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		// make sure that the object mapper stores type information when serializing objects
		objectMapper.enableDefaultTyping();
	}
}
