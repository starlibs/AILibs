package ai.libs.jaicore.search.algorithms.standard.lds;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class NoMoreNodesOnLevelEvent extends AAlgorithmEvent {

	public NoMoreNodesOnLevelEvent(final String algorithmId) {
		super(algorithmId);
	}

}
