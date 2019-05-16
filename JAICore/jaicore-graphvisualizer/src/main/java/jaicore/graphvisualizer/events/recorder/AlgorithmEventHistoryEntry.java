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

}
