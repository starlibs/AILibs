package ai.libs.jaicore.graphvisualizer.events.graph.bus;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventListener {

	@Subscribe
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException;
}
