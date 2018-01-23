package jaicore.search.algorithms.standard.core;

import java.util.List;

public class SolutionFoundEvent<T, V> {
	private final List<T> solution;
	private final V f;

	public SolutionFoundEvent(List<T> solution, V f) {
		super();
		this.solution = solution;
		this.f = f;
	}

	public List<T> getSolution() {
		return solution;
	}

	public V getF() {
		return f;
	}
}
