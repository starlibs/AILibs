package ai.libs.jaicore.graphvisualizer.events.recorder.property;

import org.api4.java.algorithm.events.IAlgorithmEvent;

public interface AlgorithmEventPropertyComputer {

	public Object computeAlgorithmEventProperty(IAlgorithmEvent algorithmEvent) throws PropertyComputationFailedException;

	public String getPropertyName();
}
