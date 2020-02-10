package ai.libs.hyperopt.util;

import ai.libs.hyperopt.event.PCSBasedOptimizationEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;

public class PCSBasedOptimizationEventListener implements AlgorithmEventListener {

	@Override
	public void handleAlgorithmEvent(final AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		PCSBasedOptimizationEvent event = (PCSBasedOptimizationEvent) algorithmEvent;

		System.out.println("event score: " + event.getScore());

	}

}
