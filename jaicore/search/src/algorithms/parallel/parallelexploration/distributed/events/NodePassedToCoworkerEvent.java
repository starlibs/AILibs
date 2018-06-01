package jaicore.search.algorithms.parallel.parallelexploration.distributed.events;

public class NodePassedToCoworkerEvent<T> {
	private final T node;

	public NodePassedToCoworkerEvent(T node) {
		super();
		this.node = node;
	}

	public T getNode() {
		return node;
	}}
