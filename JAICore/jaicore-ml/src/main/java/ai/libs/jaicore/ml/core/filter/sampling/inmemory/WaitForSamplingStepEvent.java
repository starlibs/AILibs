package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class WaitForSamplingStepEvent extends AAlgorithmEvent {

	public WaitForSamplingStepEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}

}
