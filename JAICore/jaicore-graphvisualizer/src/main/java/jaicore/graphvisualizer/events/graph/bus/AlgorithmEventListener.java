package jaicore.graphvisualizer.events.graph.bus;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventListener {

	@Subscribe
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException;
}
