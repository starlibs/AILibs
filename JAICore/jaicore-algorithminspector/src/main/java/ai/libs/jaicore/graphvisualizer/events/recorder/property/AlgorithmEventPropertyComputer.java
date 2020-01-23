package ai.libs.jaicore.graphvisualizer.events.recorder.property;

import java.util.List;

import org.api4.java.algorithm.events.IAlgorithmEvent;

public interface AlgorithmEventPropertyComputer {

	public List<AlgorithmEventPropertyComputer> getRequiredPropertyComputers();

	/**
	 * Overwrites (one of the) built-in required property computers of this property computer.
	 *
	 * @param computer
	 */
	public void overwriteRequiredPropertyComputer(AlgorithmEventPropertyComputer computer);

	public Object computeAlgorithmEventProperty(IAlgorithmEvent algorithmEvent) throws PropertyComputationFailedException;

	public String getPropertyName();
}
