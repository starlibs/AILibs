package ai.libs.jaicore.search.probleminputs;

import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.model.travesaltree.Node;

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
public class GraphSearchWithSubpathEvaluationsInput<N, A, V extends Comparable<V>> extends GraphSearchWithPathEvaluationsInput<N, A, V> {
	private final INodeEvaluator<N, V> nodeEvaluator;

	public GraphSearchWithSubpathEvaluationsInput(final GraphGenerator<N, A> graphGenerator, final INodeEvaluator<N, V> nodeEvaluator) {
		super(graphGenerator, p -> {
			try {
				return nodeEvaluator.f(new Node<>(null, p.getNodes().get(p.getNodes().size() - 1)));
			} catch (NodeEvaluationException e) {
				throw new ObjectEvaluationFailedException("Could not evaluate path", e);
			}
		});
		this.nodeEvaluator = nodeEvaluator;
	}

	public INodeEvaluator<N, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}
}
