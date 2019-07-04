package ai.libs.jaicore.search.algorithms.standard.lds;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

public class NoMoreNodesOnLevelEvent extends AAlgorithmEvent {

	public NoMoreNodesOnLevelEvent(String algorithmId) {
		super(algorithmId);
	}

}
