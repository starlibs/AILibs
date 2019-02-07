package jaicore.graphvisualizer.events.graph.bus;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventBus extends AlgorithmEventSource {

	public void postEvent(AlgorithmEvent algorithmEvent);

}
