package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class LastEventBeforeTermination extends AAlgorithmEvent {

	public LastEventBeforeTermination(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}

}
