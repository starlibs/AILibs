package ai.libs.mlplan.core.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class MLPlanPhaseSwitchedEvent extends AAlgorithmEvent {

	public MLPlanPhaseSwitchedEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}
}
