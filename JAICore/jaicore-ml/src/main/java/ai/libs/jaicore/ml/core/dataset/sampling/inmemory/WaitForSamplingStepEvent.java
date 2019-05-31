package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

public class WaitForSamplingStepEvent extends AAlgorithmEvent {

	public WaitForSamplingStepEvent(String algorithmId) {
		super(algorithmId);
	}

}
