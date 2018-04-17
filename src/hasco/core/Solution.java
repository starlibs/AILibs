package hasco.core;

import jaicore.planning.algorithms.IPlanningSolution;

/**
 * This is a wrapper class only used for efficient processing of solutions. For example, to lookup the annotations of a solution, we do not need the possibly costly equals method of T but only this
 * class. For each solution, only one such object is created.
 * 
 * @author fmohr
 *
 * @param <T>
 */
public class Solution<R extends IPlanningSolution,T> {

	private final R planningSolution;
	private final T solution;

	public Solution(R planningSolution, T solution) {
		super();
		this.planningSolution = planningSolution;
		this.solution = solution;
	}

	public R getPlanningSolution() {
		return planningSolution;
	}

	public T getSolution() {
		return solution;
	}

}
