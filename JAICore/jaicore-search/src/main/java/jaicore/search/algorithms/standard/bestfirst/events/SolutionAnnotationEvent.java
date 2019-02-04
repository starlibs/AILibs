package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.search.model.other.EvaluatedSearchGraphPath;

public class SolutionAnnotationEvent<T, A, V extends Comparable<V>> extends BestFirstEvent {

	private final EvaluatedSearchGraphPath<T,A,V> solution;
	private final String annotationName;
	private final Object annotationValue;

	public SolutionAnnotationEvent(EvaluatedSearchGraphPath<T,A,V> solution, String annotationName, Object annotationValue) {
		super();
		this.solution = solution;
		this.annotationName = annotationName;
		this.annotationValue = annotationValue;
	}

	public EvaluatedSearchGraphPath<T,A,V> getSolution() {
		return solution;
	}

	public String getAnnotationName() {
		return annotationName;
	}

	public Object getAnnotationValue() {
		return annotationValue;
	}
}
