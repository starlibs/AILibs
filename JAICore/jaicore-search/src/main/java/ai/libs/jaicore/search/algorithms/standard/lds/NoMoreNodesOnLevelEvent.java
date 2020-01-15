package ai.libs.jaicore.search.algorithms.standard.lds;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class NoMoreNodesOnLevelEvent extends AAlgorithmEvent {

	public NoMoreNodesOnLevelEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}

}
