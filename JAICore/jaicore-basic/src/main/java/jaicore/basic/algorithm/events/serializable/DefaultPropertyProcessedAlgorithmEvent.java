package jaicore.basic.algorithm.events.serializable;

import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public class DefaultPropertyProcessedAlgorithmEvent implements PropertyProcessedAlgorithmEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPropertyProcessedAlgorithmEvent.class);

	private String eventName;
	private String completeOriginalEventName;
	private Map<String, Object> properties;

	private transient AlgorithmEvent originalAlgorithmEvent;

	private long timestampOfEvent;

	@SuppressWarnings("unused")
	private DefaultPropertyProcessedAlgorithmEvent() {
		// for serialization purposes
	}

	public DefaultPropertyProcessedAlgorithmEvent(String eventName, String completeOriginalEventName, Map<String, Object> properties, long timestampOfEvent) {
		this.eventName = eventName;
		this.completeOriginalEventName = completeOriginalEventName;
		this.properties = properties;
		this.timestampOfEvent = timestampOfEvent;
	}

	public DefaultPropertyProcessedAlgorithmEvent(String eventName, Map<String, Object> properties, AlgorithmEvent originalAlgorithmEvent, long timestampOfEvent) {
		this(eventName, originalAlgorithmEvent.getClass().getName(), properties, timestampOfEvent);
		this.originalAlgorithmEvent = originalAlgorithmEvent;
	}

	@Override
	public String getEventName() {
		return eventName;
	}

	@Override
	public String getCompleteOriginalEventName() {
		return completeOriginalEventName;
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

	@Override
	public boolean correspondsToEventOfClass(Class<?> eventClass) {
		return eventClass.getSimpleName().equals(getEventName()) || eventClass.isAssignableFrom(getClassOfOriginalEvent());
	}

	@Override
	public long getTimestampOfEvent() {
		return timestampOfEvent;
	}

	private Class<?> getClassOfOriginalEvent() {
		try {
			Class<?> classOfOriginalEvent = Class.forName(getCompleteOriginalEventName());
			return classOfOriginalEvent;
		} catch (ClassNotFoundException e) {
			LOGGER.warn("Cannot find class with name {}.", getCompleteOriginalEventName(), e);
			return null;
		}
	}

}
