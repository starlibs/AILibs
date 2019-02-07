package jaicore.graphvisualizer.events.graph.bus;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.graph.GraphEvent;

public interface GraphEventListener {

	@Subscribe
	public void handleGraphEvent(GraphEvent graphEvent) throws HandleGraphEventException;
}
