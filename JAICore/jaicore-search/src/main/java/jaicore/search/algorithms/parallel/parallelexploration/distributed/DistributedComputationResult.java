package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.io.Serializable;
import java.util.Collection;

import jaicore.search.model.travesaltree.Node;

@SuppressWarnings("serial")
public class DistributedComputationResult<T, V extends Comparable<V>> implements Serializable {

	private final String coworker;
	private final Collection<Node<T, V>> open;
	private final Collection<Node<T, V>> solutions;

	public DistributedComputationResult(String coworker, Collection<Node<T, V>> open, Collection<Node<T, V>> solutions) {
		super();
		this.coworker = coworker;
		this.open = open;
		this.solutions = solutions;
	}

	public String getCoworker() {
		return coworker;
	}

	public Collection<Node<T, V>> getOpen() {
		return open;
	}

	public Collection<Node<T, V>> getSolutions() {
		return solutions;
	}
}
