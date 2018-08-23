package hasco.events;

import hasco.core.Solution;
import jaicore.planning.algorithms.IPlanningSolution;

public class HASCOSolutionEvent<R extends IPlanningSolution, T, V> {

	private Solution<R, T, ? extends Comparable<V>> solution;

	public HASCOSolutionEvent(final Solution<R, T, ? extends Comparable<V>> solution) {
		this.solution = solution;
	}

	public Solution<R, T, ? extends Comparable<V>> getSolution() {
		return this.solution;
	}

}
