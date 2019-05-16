package jaicore.graphvisualizer.events.recorder.property;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public interface AlgorithmEventPropertyComputer {

	public Object computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException;

	public String getPropertyName();
}
