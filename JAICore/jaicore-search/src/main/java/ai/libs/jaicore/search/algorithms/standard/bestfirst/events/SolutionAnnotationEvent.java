package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;

public class SolutionAnnotationEvent<T, A, V extends Comparable<V>> extends BestFirstEvent {

	private final EvaluatedSearchGraphPath<T,A,V> solution;
	private final String annotationName;
	private final Object annotationValue;

	public SolutionAnnotationEvent(final IAlgorithm<?, ?> algorithm, final EvaluatedSearchGraphPath<T,A,V> solution, final String annotationName, final Object annotationValue) {
		super(algorithm);
		this.solution = solution;
		this.annotationName = annotationName;
		this.annotationValue = annotationValue;
	}

	public EvaluatedSearchGraphPath<T,A,V> getSolution() {
		return this.solution;
	}

	public String getAnnotationName() {
		return this.annotationName;
	}

	public Object getAnnotationValue() {
		return this.annotationValue;
	}
}
