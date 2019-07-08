package ai.libs.jaicore.graphvisualizer.events.graph.bus;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventBus extends AlgorithmEventSource {

	public void postEvent(AlgorithmEvent algorithmEvent);

}
