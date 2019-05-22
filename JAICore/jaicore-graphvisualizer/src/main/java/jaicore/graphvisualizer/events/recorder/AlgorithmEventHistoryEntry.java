package jaicore.graphvisualizer.events.recorder;

import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;

public class AlgorithmEventHistoryEntry {

	private PropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent;
	private long timeEventWasReceived;

	@SuppressWarnings("unused")
	private AlgorithmEventHistoryEntry() {
		// for serialization purposes
	}

	public AlgorithmEventHistoryEntry(PropertyProcessedAlgorithmEvent algorithmEvent, long timeEventWasReceived) {
		this.propertyProcessedAlgorithmEvent = algorithmEvent;
		this.timeEventWasReceived = timeEventWasReceived;
	}

	public PropertyProcessedAlgorithmEvent getAlgorithmEvent() {
		return propertyProcessedAlgorithmEvent;
	}

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
