package ai.libs.jaicore.graphvisualizer.events.graph.bus;

import org.api4.java.algorithm.events.IAlgorithmEvent;

public interface AlgorithmEventBus extends AlgorithmEventSource {

	public void postEvent(IAlgorithmEvent algorithmEvent);

}
