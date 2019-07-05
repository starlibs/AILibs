package ai.libs.jaicore.basic.algorithm.events.serializable;

import java.io.Serializable;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;

/**
 * A {@link PropertyProcessedAlgorithmEvent} are constructed based on an {@link AlgorithmEvent} where the most important information is extracted in stored in the form of one or more properties. It is serializable in the sense that it
 * should only contain properties which can be serialized to JSON.
 * 
 * @author atornede
 *
 */
public interface PropertyProcessedAlgorithmEvent extends Serializable {

	/**
	 * Returns the name of the underlying {@link AlgorithmEvent} on the basis this {@link PropertyProcessedAlgorithmEvent} was created.
	 * 
	 * @return The name of the underlying {@link AlgorithmEvent}.
	 */
	public String getEventName();

	/**
	 * Returns the fully qualified name of the underlying {@link AlgorithmEvent} on the basis this {@link PropertyProcessedAlgorithmEvent} was created.
	 * 
	 * @return The fully qualified name of the underlying {@link AlgorithmEvent}.
	 */
	public String getCompleteOriginalEventName();

	/**
	 * Returns the property with the given name.
	 * 
	 * @param propertyName The name of the property to be returned.
	 * @return The property with the given name.
	 */
	public Object getProperty(String propertyName);

	/**
	 * Returns the property with the given name, assuming it is an instance of the given {@link Class}.
	 * 
	 * @param propertyName The name of the property to be returned.
	 * @param expectedClassToBeReturned The {@link Class} underlying the instance of the property to be returned.
	 * @return The property with the given name.
	 * @throws ClassCastException If the given {@link Class} does not fit the {@link Class} of the instance of the property returned.
	 */
	public <N> N getProperty(String propertyName, Class<N> expectedClassToBeReturned) throws ClassCastException;

	/**
	 * Returns the original {@link AlgorithmEvent} from which this one was constructed. Note: This may be {@code null} in certain cases, for example if this instance was created from a serialized {@link String}.
	 * 
	 * @return The original {@link AlgorithmEvent} from which this one was constructed or {@code null} if it is not available.
	 */
	public AlgorithmEvent getOriginalEvent();

	/**
	 * Checks whether this event corresponds to the given {@link AlgorithmEvent} class.
	 * 
	 * @param eventClass The {@link AlgorithmEvent} class to check against.
	 * @return {@code true} if this {@link PropertyProcessedAlgorithmEvent} corresponds to the given {@link AlgorithmEvent} class, {@code false} otherwise
	 */
	public boolean correspondsToEventOfClass(Class<? extends AlgorithmEvent> eventClass);

	/**
	 * Returns the timestamp of this event.
	 * 
	 * @return The timestamp of this event.
	 */
	public long getTimestampOfEvent();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
