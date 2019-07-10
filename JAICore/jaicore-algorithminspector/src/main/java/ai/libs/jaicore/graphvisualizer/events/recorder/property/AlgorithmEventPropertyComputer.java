package ai.libs.jaicore.graphvisualizer.events.recorder.property;

import org.api4.java.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventPropertyComputer {

	public Object computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException;

	public String getPropertyName();
}
