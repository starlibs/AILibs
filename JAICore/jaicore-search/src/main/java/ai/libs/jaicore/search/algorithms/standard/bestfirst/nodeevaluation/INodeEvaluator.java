package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.model.travesaltree.Node;

public interface INodeEvaluator<T,V extends Comparable<V>> {
	public V f(Node<T,?> node) throws NodeEvaluationException, InterruptedException;
}
