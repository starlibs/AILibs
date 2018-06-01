package jaicore.search.algorithms.standard.core;

import java.util.List;

public class SolutionAnnotationEvent<T, V extends Comparable<V>> {

	private final List<T> solution;
	private final String annotationName;
	private final Object annotationValue;

	public SolutionAnnotationEvent(List<T> solution, String annotationName, Object annotationValue) {
		super();
		this.solution = solution;
		this.annotationName = annotationName;
		this.annotationValue = annotationValue;
	}

	public List<T> getSolution() {
		return solution;
	}

	public String getAnnotationName() {
		return annotationName;
	}

	public Object getAnnotationValue() {
		return annotationValue;
	}
}
