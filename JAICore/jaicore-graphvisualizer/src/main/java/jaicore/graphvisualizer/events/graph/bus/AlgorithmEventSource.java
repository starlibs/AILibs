package jaicore.graphvisualizer.events.graph.bus;

public interface AlgorithmEventSource {

	public void registerListener(AlgorithmEventListener algorithmEventListener);

	public void unregisterListener(AlgorithmEventListener algorithmEventListener);

}
