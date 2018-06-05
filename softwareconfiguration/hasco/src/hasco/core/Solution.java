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
public class Solution<R extends IPlanningSolution, T, V extends Comparable<V>> {

	private final R planningSolution;
	private final T solution;
	private final V score;
	private final int timeToComputeScore;

	public Solution(R planningSolution, T solution, V score, int timeToComputeScore) {
		super();
		if (score == null)
			throw new IllegalArgumentException("Obtained solution without score!");
		this.planningSolution = planningSolution;
		this.solution = solution;
		this.score = score;
		this.timeToComputeScore = timeToComputeScore;
	}
	
	public Solution(Solution<R,T,V> s) {
		this(s.planningSolution, s.solution, s.score, s.timeToComputeScore);
	}

	public R getPlanningSolution() {
		return planningSolution;
	}

	public T getSolution() {
		return solution;
	}

	public V getScore() {
		return score;
	}

	public int getTimeToComputeScore() {
		return timeToComputeScore;
	}
}
