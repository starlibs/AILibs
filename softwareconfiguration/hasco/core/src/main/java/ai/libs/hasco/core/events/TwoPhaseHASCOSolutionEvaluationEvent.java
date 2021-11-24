package ai.libs.hasco.core.events;

import ai.libs.jaicore.components.model.ComponentInstance;

public class TwoPhaseHASCOSolutionEvaluationEvent<T, V extends Comparable<V>> {
	private final ComponentInstance componentInstance;
	private final T solution;
	private final V score;

	public TwoPhaseHASCOSolutionEvaluationEvent(final ComponentInstance componentInstance, final T solution, final V score) {
		super();
		this.componentInstance = componentInstance;
		this.solution = solution;
		this.score = score;
	}

	public ComponentInstance getComponentInstance() {
		return this.componentInstance;
	}

	public T getSolution() {
		return this.solution;
	}

	public V getScore() {
		return this.score;
	}
}
