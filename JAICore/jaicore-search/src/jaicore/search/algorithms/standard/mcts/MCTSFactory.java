package jaicore.search.algorithms.standard.mcts;

import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.probleminputs.GraphSearchProblemInput;
import jaicore.search.model.travesaltree.Node;

public class MCTSFactory<N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<GraphSearchProblemInput<N, A, V>, Object, N, A, V, Node<N, V>, A> {
	private IPathUpdatablePolicy<N, A, V> treePolicy;
	private IPolicy<N, A, V> defaultPolicy;

	public IPathUpdatablePolicy<N, A, V> getTreePolicy() {
		return treePolicy;
	}

	public void setTreePolicy(IPathUpdatablePolicy<N, A, V> treePolicy) {
		this.treePolicy = treePolicy;
	}

	public IPolicy<N, A, V> getDefaultPolicy() {
		return defaultPolicy;
	}

	public void setDefaultPolicy(IPolicy<N, A, V> defaultPolicy) {
		this.defaultPolicy = defaultPolicy;
	}

	@Override
	public MCTS<N, A, V> getAlgorithm() {
		return new MCTS<>(getProblemInput(), treePolicy, defaultPolicy);
	}
}
