package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class WaitForSamplingStepEvent extends AAlgorithmEvent {

	public WaitForSamplingStepEvent(final String algorithmId) {
		super(algorithmId);
	}

}
