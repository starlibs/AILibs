package ai.libs.hasco.serialization;

import java.util.Collection;

import com.fasterxml.jackson.databind.module.SimpleModule;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;

public class HASCOJacksonModule extends SimpleModule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HASCOJacksonModule(Collection<Component> components) {
		super();
		this.addDeserializer(ComponentInstance.class, new ComponentInstanceDeserializer(components));
	}

}
