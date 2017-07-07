package jaicore.search.algorithms.parallel.parallelevaluation.local.core;

public class NodeValueComputationTimeoutEvent<T> {

	private final T node;

	public NodeValueComputationTimeoutEvent(T node) {
		super();
		this.node = node;
	}

	public T getNode() {
		return node;
	}
}
