package jaicore.graphvisualizer.events.graph.bus;

import jaicore.graphvisualizer.events.graph.GraphEvent;

public interface GraphEventBus extends GraphEventSource {

	public void postEvent(GraphEvent graphEvent);

}
