package ai.libs.hasco.core.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class TwoPhaseHASCOSelectionPhaseSkippedEvent extends AAlgorithmEvent {

	public TwoPhaseHASCOSelectionPhaseSkippedEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}
}
