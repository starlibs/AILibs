package hasco.events;

import hasco.model.ComponentInstance;

public class HASCORunTerminatedEvent<T, V extends Comparable<V>> {

	private final T returnedSolution;
	private final ComponentInstance compositionOfSolution;
	private final V score;

	public HASCORunTerminatedEvent(ComponentInstance composition, T returnedSolution, V score) {
		super();
		this.compositionOfSolution = composition;
		this.returnedSolution = returnedSolution;
		this.score = score;
	}

	public ComponentInstance getCompositionOfSolution() {
		return compositionOfSolution;
	}

	public T getReturnedSolution() {
		return returnedSolution;
	}

	public V getScore() {
		return score;
	}
}
