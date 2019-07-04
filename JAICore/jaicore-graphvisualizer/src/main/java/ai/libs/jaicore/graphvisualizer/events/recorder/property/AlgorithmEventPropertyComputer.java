package ai.libs.jaicore.graphvisualizer.events.recorder.property;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventPropertyComputer {

	public Object computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException;

	public String getPropertyName();
}
