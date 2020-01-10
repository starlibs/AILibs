package ai.libs.hasco.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class TwoPhaseHASCOPhaseSwitchEvent extends AAlgorithmEvent {

	public TwoPhaseHASCOPhaseSwitchEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}
}
