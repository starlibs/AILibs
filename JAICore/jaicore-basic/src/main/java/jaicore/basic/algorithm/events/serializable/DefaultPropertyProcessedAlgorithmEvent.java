package jaicore.basic.algorithm.events.serializable;

import java.util.Map;
import java.util.NoSuchElementException;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public class DefaultPropertyProcessedAlgorithmEvent implements PropertyProcessedAlgorithmEvent {

	private String eventName;
	private Map<String, Object> properties;

	private transient AlgorithmEvent originalAlgorithmEvent;

	@SuppressWarnings("unused")
	private DefaultPropertyProcessedAlgorithmEvent() {
		// for serialization purposes
	}

	public DefaultPropertyProcessedAlgorithmEvent(String eventName, Map<String, Object> properties) {
		this.eventName = eventName;
		this.properties = properties;
	}

	public DefaultPropertyProcessedAlgorithmEvent(String eventName, Map<String, Object> properties, AlgorithmEvent originalAlgorithmEvent) {
		this(eventName, properties);
		this.originalAlgorithmEvent = originalAlgorithmEvent;
	}

	@Override
	public String getEventName() {
		return eventName;
	}

	@Override
	public Object getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public <N> N getProperty(String propertyName, Class<N> expectedClassToBeReturned) {

		Object property = properties.get(propertyName);
		if (property == null) {
			throw new NoSuchElementException("No property with name \"" + propertyName + "\" present.");
		}
		if (!expectedClassToBeReturned.isInstance(property)) {
			throw new ClassCastException("Property with name \"" + propertyName + "\" is not of type: " + expectedClassToBeReturned.getName());
		}
		return (N) property;
	}

	@Override
	public AlgorithmEvent getOriginalEvent() {
		return originalAlgorithmEvent;
	}

}
