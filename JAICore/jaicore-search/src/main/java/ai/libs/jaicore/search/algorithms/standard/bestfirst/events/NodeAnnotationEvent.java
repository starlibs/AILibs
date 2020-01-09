package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;

public class NodeAnnotationEvent<T> extends BestFirstEvent {

	private final T node;
	private final String annotationName;
	private final Object annotationValue;

	public NodeAnnotationEvent(final IAlgorithm<?, ?> algorithm, final T node, final String annotationName, final Object annotationValue) {
		super(algorithm);
		this.node = node;
		this.annotationName = annotationName;
		this.annotationValue = annotationValue;
	}

	public T getNode() {
		return this.node;
	}

	public String getAnnotationName() {
		return this.annotationName;
	}

	public Object getAnnotationValue() {
		return this.annotationValue;
	}
}
