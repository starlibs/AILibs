package util.search.distributed;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import util.search.core.Node;

public class DistributedComputationResult<T, V extends Comparable<V>> implements Serializable {
	private static final long serialVersionUID = -3947093065171620081L;

	private final Collection<List<Node<T, V>>> open;
	private final Collection<List<Node<T, V>>> solutions;

	public DistributedComputationResult(Collection<List<Node<T, V>>> open, Collection<List<Node<T, V>>> solutions) {
		super();
		this.open = open;
		this.solutions = solutions;
	}

	public Collection<List<Node<T, V>>> getOpen() {
		return open;
	}

	public Collection<List<Node<T, V>>> getSolutions() {
		return solutions;
	}
}
