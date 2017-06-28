package util.search.bestfirst;

import java.util.List;

import util.search.core.NodeEvaluator;
import util.search.core.Node;

public interface BestFirstOracle<T,V extends Comparable<V>> extends NodeEvaluator<T,V> {
	public List<Node<T,V>> getBestPathCompletionEstimate(Node<T,V> node);
}
