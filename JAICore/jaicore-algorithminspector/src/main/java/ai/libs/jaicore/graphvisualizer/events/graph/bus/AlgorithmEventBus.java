package ai.libs.jaicore.graphvisualizer.events.graph.bus;

import org.api4.java.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventBus extends AlgorithmEventSource {

	public void postEvent(AlgorithmEvent algorithmEvent);

}
