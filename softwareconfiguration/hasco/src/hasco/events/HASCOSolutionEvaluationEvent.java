package hasco.events;

import hasco.model.ComponentInstance;

public class HASCOSolutionEvaluationEvent<T, V extends Comparable<V>> {
	private final ComponentInstance composition;
	private final T solution;
	private final V score;

	public HASCOSolutionEvaluationEvent(ComponentInstance composition, T solution, V score) {
		super();
		this.composition = composition;
		this.solution = solution;
		this.score = score;
	}

	public ComponentInstance getComposition() {
		return composition;
	}

	public T getSolution() {
		return solution;
	}

	public V getScore() {
		return score;
	}
}
