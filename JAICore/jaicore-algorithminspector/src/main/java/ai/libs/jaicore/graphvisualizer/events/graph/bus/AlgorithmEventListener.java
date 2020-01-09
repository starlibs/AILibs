package ai.libs.jaicore.graphvisualizer.events.graph.bus;

import org.api4.java.algorithm.events.IAlgorithmEvent;

import com.google.common.eventbus.Subscribe;

public interface AlgorithmEventListener {

	@Subscribe
	public void handleAlgorithmEvent(IAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException;
}
