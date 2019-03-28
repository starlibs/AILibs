package jaicore.ml.core.dataset.sampling.inmemory;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class WaitForSamplingStepEvent extends AAlgorithmEvent {

	public WaitForSamplingStepEvent(String algorithmId) {
		super(algorithmId);
	}

}
