package jaicore.graphvisualizer.events.recorder;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.graph.GraphEvent;
import jaicore.graphvisualizer.events.graph.bus.GraphEventListener;

public class GraphEventHistoryRecorder implements GraphEventListener {

	private GraphEventHistory graphEventHistory;

	public GraphEventHistoryRecorder() {
		graphEventHistory = new GraphEventHistory();
	}

	@Subscribe
	@Override
	public void handleGraphEvent(GraphEvent graphEvent) {
		synchronized (this) {
			graphEventHistory.addEvent(graphEvent);
		}
	}

	public GraphEventHistory getHistory() {
		return graphEventHistory;
	}

}
