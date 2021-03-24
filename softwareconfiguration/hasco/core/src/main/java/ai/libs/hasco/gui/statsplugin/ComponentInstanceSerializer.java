package ai.libs.hasco.gui.statsplugin;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;

public class ComponentInstanceSerializer {

	private ObjectMapper objectMapper;

	public ComponentInstanceSerializer() {
		this.initializeObjectMapper();
	}

	public String serializeComponentInstance(final IComponentInstance componentInstance) throws JsonProcessingException {
		return this.objectMapper.writeValueAsString(componentInstance);
	}

	public ComponentInstance deserializeComponentInstance(final String serializedComponentInstance) throws IOException {
		return this.objectMapper.readValue(serializedComponentInstance, ComponentInstance.class);
	}

	private void initializeObjectMapper() {
		this.objectMapper = new ObjectMapper();

		this.objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		this.objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		// make sure that the object mapper stores type information when serializing objects
		this.objectMapper.enableDefaultTyping();
	}
}
