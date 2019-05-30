package jaicore.search.probleminputs;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;

/**
 * Many algorithms such as best first and A* use a traversal tree to browse the underlying
 * graph. Each node in this tree corresponds to a node in the original graph but has only
 * one predecessor, which may be updated over time.
 * 
 * The underlying class Node<T,V> implicitly defines a back pointer PATH from the node to
 * the root. Therefore, evaluating a node of this class equals evaluating a path in the
 * original graph.
 * 
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class GraphSearchWithSubpathEvaluationsInput<N, A, V extends Comparable<V>> extends GraphSearchInput<N, A> {
	private final INodeEvaluator<N, V> nodeEvaluator;

	public GraphSearchWithSubpathEvaluationsInput(GraphGenerator<N, A> graphGenerator, INodeEvaluator<N, V> nodeEvaluator) {
		super(graphGenerator);
		this.nodeEvaluator = nodeEvaluator;
	}

	public INodeEvaluator<N, V> getNodeEvaluator() {
		return nodeEvaluator;
	}
}
