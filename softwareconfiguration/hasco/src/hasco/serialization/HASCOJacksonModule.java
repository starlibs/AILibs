package hasco.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;

import hasco.model.ComponentInstance;

public class HASCOJacksonModule extends SimpleModule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HASCOJacksonModule() {
		super();
		this.addDeserializer(ComponentInstance.class, new ComponentInstanceDeserializer());
	}

}
