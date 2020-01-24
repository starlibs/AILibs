package ai.libs.jaicore.ml.core.filter.sampling;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class SampleElementAddedEvent extends AAlgorithmEvent {

	public SampleElementAddedEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}

}
