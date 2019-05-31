package ai.libs.jaicore.ml.core.dataset.sampling;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

public class SampleElementAddedEvent extends AAlgorithmEvent {

	public SampleElementAddedEvent(String algorithmId) {
		super(algorithmId);
	}

}
