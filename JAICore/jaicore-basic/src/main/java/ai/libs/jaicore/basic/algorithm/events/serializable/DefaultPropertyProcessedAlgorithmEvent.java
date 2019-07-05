package ai.libs.jaicore.basic.algorithm.events.serializable;

import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;

public class DefaultPropertyProcessedAlgorithmEvent implements PropertyProcessedAlgorithmEvent {

	private static final long serialVersionUID = -6645533957593455739L;

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

	@SuppressWarnings("unchecked")
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
	public boolean correspondsToEventOfClass(Class<? extends AlgorithmEvent> eventClass) {
		return eventClass.getSimpleName().equals(getEventName()) || eventClass.isAssignableFrom(getClassOfOriginalEvent());
	}

	@Override
	public long getTimestampOfEvent() {
		return timestampOfEvent;
	}

	private Class<?> getClassOfOriginalEvent() {
		try {
			return Class.forName(getCompleteOriginalEventName());
		} catch (ClassNotFoundException e) {
			LOGGER.warn("Cannot find class with name {}.", getCompleteOriginalEventName(), e);
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((completeOriginalEventName == null) ? 0 : completeOriginalEventName.hashCode());
		result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + (int) (timestampOfEvent ^ (timestampOfEvent >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DefaultPropertyProcessedAlgorithmEvent other = (DefaultPropertyProcessedAlgorithmEvent) obj;
		if (completeOriginalEventName == null) {
			if (other.completeOriginalEventName != null) {
				return false;
			}
		} else if (!completeOriginalEventName.equals(other.completeOriginalEventName)) {
			return false;
		}
		if (eventName == null) {
			if (other.eventName != null) {
				return false;
			}
		} else if (!eventName.equals(other.eventName)) {
			return false;
		}
		if (properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else if (!properties.equals(other.properties)) {
			return false;
		}
		if (timestampOfEvent != other.timestampOfEvent) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DefaultPropertyProcessedAlgorithmEvent [eventName=" + eventName + ", completeOriginalEventName=" + completeOriginalEventName + ", properties=" + properties + ", timestampOfEvent=" + timestampOfEvent + "]";
	}

}
