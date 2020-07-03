package ai.libs.hasco.events;

import ai.libs.jaicore.components.model.ComponentInstance;

public class HASCORunTerminatedEvent<T, V extends Comparable<V>> {

	private final T returnedSolution;
	private final ComponentInstance compositionOfSolution;
	private final V score;

	public HASCORunTerminatedEvent(final ComponentInstance composition, final T returnedSolution, final V score) {
		super();
		this.compositionOfSolution = composition;
		this.returnedSolution = returnedSolution;
		this.score = score;
	}

	public ComponentInstance getCompositionOfSolution() {
		return this.compositionOfSolution;
	}

	public T getReturnedSolution() {
		return this.returnedSolution;
	}

	public V getScore() {
		return this.score;
	}
}
