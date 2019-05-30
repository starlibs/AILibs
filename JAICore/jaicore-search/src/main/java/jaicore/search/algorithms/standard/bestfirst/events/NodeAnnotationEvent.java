package jaicore.search.algorithms.standard.bestfirst.events;

public class NodeAnnotationEvent<T> extends BestFirstEvent {

	private final T node;
	private final String annotationName;
	private final Object annotationValue;

	public NodeAnnotationEvent(String algorithmId, T node, String annotationName, Object annotationValue) {
		super(algorithmId);
		this.node = node;
		this.annotationName = annotationName;
		this.annotationValue = annotationValue;
	}

	public T getNode() {
		return node;
	}

	public String getAnnotationName() {
		return annotationName;
	}

	public Object getAnnotationValue() {
		return annotationValue;
	}
}
