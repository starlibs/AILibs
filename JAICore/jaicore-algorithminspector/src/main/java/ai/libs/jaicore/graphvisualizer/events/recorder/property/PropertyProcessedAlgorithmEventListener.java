package ai.libs.jaicore.graphvisualizer.events.recorder.property;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;

public interface PropertyProcessedAlgorithmEventListener {

	public void handleSerializableAlgorithmEvent(IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException;
}
