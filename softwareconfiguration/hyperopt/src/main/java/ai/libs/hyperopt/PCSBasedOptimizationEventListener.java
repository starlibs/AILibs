package ai.libs.hyperopt;

import org.api4.java.algorithm.events.IAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;

public class PCSBasedOptimizationEventListener implements AlgorithmEventListener {

	@Override
	public void handleAlgorithmEvent(final IAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		PCSBasedOptimizationEvent event = (PCSBasedOptimizationEvent) algorithmEvent;
		throw new UnsupportedOperationException("Not implemented. This does nothing with the given event " + event);
	}

}
