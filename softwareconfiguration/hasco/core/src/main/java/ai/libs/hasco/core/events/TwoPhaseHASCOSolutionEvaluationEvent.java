package ai.libs.hasco.core.events;

import ai.libs.hasco.twophase.TwoPhaseHASCO;
import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;
import ai.libs.jaicore.components.api.IComponentInstance;

public class TwoPhaseHASCOSolutionEvaluationEvent extends AAlgorithmEvent {
	private final IComponentInstance componentInstance;
	private final double score;

	public TwoPhaseHASCOSolutionEvaluationEvent(final TwoPhaseHASCO<?, ?> algorithm, final IComponentInstance componentInstance, final double score) {
		super(algorithm);
		this.componentInstance = componentInstance;
		this.score = score;
	}

	public IComponentInstance getComponentInstance() {
		return this.componentInstance;
	}
	public double getScore() {
		return this.score;
	}
}
