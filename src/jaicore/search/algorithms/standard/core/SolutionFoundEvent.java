package jaicore.search.algorithms.standard.core;

import java.util.List;

import jaicore.search.algorithms.interfaces.solutionannotations.SolutionAnnotation;

public class SolutionFoundEvent<T, V extends Comparable<V>> {
	private final List<T> solution;
	private final SolutionAnnotation<T, V> annotation;

	public SolutionFoundEvent(List<T> solution, SolutionAnnotation<T, V> annotation) {
		super();
		this.solution = solution;
		this.annotation = annotation;
	}

	public List<T> getSolution() {
		return solution;
	}

	public SolutionAnnotation<T, V> getAnnotation() {
		return annotation;
	}
}
