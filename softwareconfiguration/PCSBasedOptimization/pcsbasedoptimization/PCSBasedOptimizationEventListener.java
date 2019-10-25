package ai.libs.hasco.pcsbasedoptimization;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;

public class PCSBasedOptimizationEventListener implements AlgorithmEventListener {

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		PCSBasedOptimizationEvent event = (PCSBasedOptimizationEvent) algorithmEvent;
		
		System.out.println("event score: " + event.getScore());
		
	}

}
