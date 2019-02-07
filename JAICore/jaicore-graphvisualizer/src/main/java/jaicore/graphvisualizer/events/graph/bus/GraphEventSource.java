package jaicore.graphvisualizer.events.graph.bus;

public interface GraphEventSource {

	public void registerListener(GraphEventListener graphEventListener);

	public void unregisterListener(GraphEventListener graphEventListener);

}
