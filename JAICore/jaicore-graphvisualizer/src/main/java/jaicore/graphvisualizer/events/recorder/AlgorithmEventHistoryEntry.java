package jaicore.graphvisualizer.events.recorder;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.GraphEvent;

public class AlgorithmEventHistoryEntry {

	private AlgorithmEvent algorithmEvent;
	private long timeEventWasReceived;

	public AlgorithmEventHistoryEntry(AlgorithmEvent algorithmEvent, long timeEventWasReceived) {
		this.algorithmEvent = algorithmEvent;
		this.timeEventWasReceived = timeEventWasReceived;
	}

	public AlgorithmEventHistoryEntry(GraphEvent graphEvent) {
		this(graphEvent, 0);
	}

	public AlgorithmEvent getAlgorithmEvent() {
		return algorithmEvent;
	}

	public long getTimeEventWasReceived() {
		return timeEventWasReceived;
	}

}
