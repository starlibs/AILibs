package ai.libs.jaicore.graphvisualizer.events.recorder;

import ai.libs.jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;

/**
 * {@link AlgorithmEventHistoryEntry}s are used to store {@link PropertyProcessedAlgorithmEvent}s in an {@link AlgorithmEventHistory} combined with additional meta information.
 * 
 * @author atornede
 *
 */
public class AlgorithmEventHistoryEntry {

	private PropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent;
	private long timeEventWasReceived;

	@SuppressWarnings("unused")
	private AlgorithmEventHistoryEntry() {
		// for serialization purposes
	}

	/**
	 * Creates a new {@link AlgorithmEventHistoryEntry} storing the given {@link PropertyProcessedAlgorithmEvent} and the time at which the event was received.
	 * 
	 * @param algorithmEvent The {@link PropertyProcessedAlgorithmEvent} to be stored.
	 * @param timeEventWasReceived The time at which the event was received.
	 */
	public AlgorithmEventHistoryEntry(PropertyProcessedAlgorithmEvent algorithmEvent, long timeEventWasReceived) {
		this.propertyProcessedAlgorithmEvent = algorithmEvent;
		this.timeEventWasReceived = timeEventWasReceived;
	}

	/**
	 * Returns the {@link PropertyProcessedAlgorithmEvent} stored as part of this entry.
	 * 
	 * @return The {@link PropertyProcessedAlgorithmEvent} stored as part of this entry.
	 */
	public PropertyProcessedAlgorithmEvent getAlgorithmEvent() {
		return propertyProcessedAlgorithmEvent;
	}

	/**
	 * Returns the time at which this event was received.
	 * 
	 * @return The time at which this event was received.
	 */
	public long getTimeEventWasReceived() {
		return timeEventWasReceived;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyProcessedAlgorithmEvent == null) ? 0 : propertyProcessedAlgorithmEvent.hashCode());
		result = prime * result + (int) (timeEventWasReceived ^ (timeEventWasReceived >>> 32));
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
		AlgorithmEventHistoryEntry other = (AlgorithmEventHistoryEntry) obj;
		if (propertyProcessedAlgorithmEvent == null) {
			if (other.propertyProcessedAlgorithmEvent != null) {
				return false;
			}
		} else if (!propertyProcessedAlgorithmEvent.equals(other.propertyProcessedAlgorithmEvent)) {
			return false;
		}
		if (timeEventWasReceived != other.timeEventWasReceived) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "<" + propertyProcessedAlgorithmEvent + ", t=" + timeEventWasReceived + ">";
	}

}
