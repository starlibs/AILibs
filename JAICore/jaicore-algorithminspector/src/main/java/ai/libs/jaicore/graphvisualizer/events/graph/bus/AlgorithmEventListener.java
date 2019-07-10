package ai.libs.jaicore.graphvisualizer.events.graph.bus;

import org.api4.java.algorithm.events.AlgorithmEvent;

import com.google.common.eventbus.Subscribe;

public interface AlgorithmEventListener {

	@Subscribe
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException;
}
