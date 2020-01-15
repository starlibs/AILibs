package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class BestFirstEvent extends AAlgorithmEvent {

	public BestFirstEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}

}
