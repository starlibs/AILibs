package jaicore.search.algorithms.standard.core;

import java.util.List;

public class SolutionFoundEvent<T> {
	private final List<T> solution;

	public SolutionFoundEvent(List<T> solution) {
		super();
		this.solution = solution;
	}

	public List<T> getSolution() {
		return solution;
	}
}
