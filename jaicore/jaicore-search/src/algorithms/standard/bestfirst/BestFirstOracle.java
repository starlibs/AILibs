package jaicore.search.algorithms.standard.bestfirst;

import java.util.List;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public interface BestFirstOracle<T,V extends Comparable<V>> extends INodeEvaluator<T,V> {
	public List<Node<T,V>> getBestPathCompletionEstimate(Node<T,V> node);
}
