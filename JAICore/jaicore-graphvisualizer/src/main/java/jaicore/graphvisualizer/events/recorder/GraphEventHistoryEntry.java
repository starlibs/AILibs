package jaicore.graphvisualizer.events.recorder;

import jaicore.graphvisualizer.events.graph.GraphEvent;

public class GraphEventHistoryEntry {

	private GraphEvent graphEvent;
	private long timeEventWasReceived;

	public GraphEventHistoryEntry(GraphEvent graphEvent, long timeEventWasReceived) {
		this.graphEvent = graphEvent;
		this.timeEventWasReceived = timeEventWasReceived;
	}

	public GraphEventHistoryEntry(GraphEvent graphEvent) {
		this(graphEvent, 0);
	}

	public GraphEvent getGraphEvent() {
		return graphEvent;
	}

	public long getTimeEventWasReceived() {
		return timeEventWasReceived;
	}

}
