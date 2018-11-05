package jaicore.search.algorithms.standard.lds;

import jaicore.graph.TreeNode;
import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.NodeRecommendedTree;

public class LimitedDiscrepancySearchFactory<T, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<NodeRecommendedTree<T, A>, EvaluatedSearchGraphPath<T, A,V>, T, A, V, TreeNode<T>, A> {

	@Override
	public LimitedDiscrepancySearch<T, A, V> getAlgorithm() {
		if (getProblemInput() == null)
			throw new IllegalArgumentException("Cannot create algorithm; problem input has not been set yet");
		return new LimitedDiscrepancySearch<T,A,V>(getProblemInput());
	}
}
