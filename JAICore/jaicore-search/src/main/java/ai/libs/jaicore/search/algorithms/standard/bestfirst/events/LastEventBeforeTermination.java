package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

public class LastEventBeforeTermination extends AAlgorithmEvent {

	public LastEventBeforeTermination(String algorithmId) {
		super(algorithmId);
	}

}
