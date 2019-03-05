package jaicore.graphvisualizer.events.recorder;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public class AlgorithmEventHistoryEntry {

	private AlgorithmEvent algorithmEvent;
	private long timeEventWasReceived;

	public AlgorithmEventHistoryEntry(AlgorithmEvent algorithmEvent, long timeEventWasReceived) {
		this.algorithmEvent = algorithmEvent;
		this.timeEventWasReceived = timeEventWasReceived;
	}

	public AlgorithmEvent getAlgorithmEvent() {
		return algorithmEvent;
	}

	public long getTimeEventWasReceived() {
		return timeEventWasReceived;
	}

}
