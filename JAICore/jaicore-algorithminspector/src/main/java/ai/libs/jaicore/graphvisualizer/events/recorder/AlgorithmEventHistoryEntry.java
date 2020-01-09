package ai.libs.jaicore.graphvisualizer.events.recorder;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

/**
 * {@link AlgorithmEventHistoryEntry}s are used to store {@link IPropertyProcessedAlgorithmEvent}s in an {@link AlgorithmEventHistory} combined with additional meta information.
 *
 * @author atornede
 *
 */
public class AlgorithmEventHistoryEntry {

	private IPropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent;
	private long timeEventWasReceived;

	@SuppressWarnings("unused")
	private AlgorithmEventHistoryEntry() {
		// for serialization purposes
	}

	/**
	 * Creates a new {@link AlgorithmEventHistoryEntry} storing the given {@link IPropertyProcessedAlgorithmEvent} and the time at which the event was received.
	 *
	 * @param algorithmEvent The {@link IPropertyProcessedAlgorithmEvent} to be stored.
	 * @param timeEventWasReceived The time at which the event was received.
	 */
	public AlgorithmEventHistoryEntry(final IPropertyProcessedAlgorithmEvent algorithmEvent, final long timeEventWasReceived) {
		this.propertyProcessedAlgorithmEvent = algorithmEvent;
		this.timeEventWasReceived = timeEventWasReceived;
	}

	/**
	 * Returns the {@link IPropertyProcessedAlgorithmEvent} stored as part of this entry.
	 *
	 * @return The {@link IPropertyProcessedAlgorithmEvent} stored as part of this entry.
	 */
	public IPropertyProcessedAlgorithmEvent getAlgorithmEvent() {
		return this.propertyProcessedAlgorithmEvent;
	}

	/**
	 * Returns the time at which this event was received.
	 *
	 * @return The time at which this event was received.
	 */
	public long getTimeEventWasReceived() {
		return this.timeEventWasReceived;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.propertyProcessedAlgorithmEvent == null) ? 0 : this.propertyProcessedAlgorithmEvent.hashCode());
		result = prime * result + (int) (this.timeEventWasReceived ^ (this.timeEventWasReceived >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		AlgorithmEventHistoryEntry other = (AlgorithmEventHistoryEntry) obj;
		if (this.propertyProcessedAlgorithmEvent == null) {
			if (other.propertyProcessedAlgorithmEvent != null) {
				return false;
			}
		} else if (!this.propertyProcessedAlgorithmEvent.equals(other.propertyProcessedAlgorithmEvent)) {
			return false;
		}
		if (this.timeEventWasReceived != other.timeEventWasReceived) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "<" + this.propertyProcessedAlgorithmEvent + ", t=" + this.timeEventWasReceived + ">";
	}

}
