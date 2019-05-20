package jaicore.basic.algorithm.events.serializable;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public interface PropertyProcessedAlgorithmEvent {

	public String getEventName();

	public String getCompleteOriginalEventName();

	public Object getProperty(String propertyName);

	public <N> N getProperty(String propertyName, Class<N> expectedClassToBeReturned) throws ClassCastException;

	public AlgorithmEvent getOriginalEvent();

	public boolean correspondsToEventOfClass(Class<?> eventClass);

	public long getTimestampOfEvent();
}
