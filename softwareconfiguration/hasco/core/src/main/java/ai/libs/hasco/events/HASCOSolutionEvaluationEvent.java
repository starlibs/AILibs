package ai.libs.hasco.events;

import ai.libs.softwareconfiguration.model.ComponentInstance;

public class HASCOSolutionEvaluationEvent<T, V extends Comparable<V>> {
	private final ComponentInstance composition;
	private final T solution;
	private final V score;

	public HASCOSolutionEvaluationEvent(final ComponentInstance composition, final T solution, final V score) {
		super();
		this.composition = composition;
		this.solution = solution;
		this.score = score;
	}

	public ComponentInstance getComposition() {
		return this.composition;
	}

	public T getSolution() {
		return this.solution;
	}

	public V getScore() {
		return this.score;
	}
}
