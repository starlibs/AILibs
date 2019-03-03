package jaicore.ml.core.dataset.sampling;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class SampleElementAddedEvent extends AAlgorithmEvent {

	public SampleElementAddedEvent(String algorithmId) {
		super(algorithmId);
	}

}
