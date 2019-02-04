package jaicore.search.algorithms.standard.lds;

import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;

public class BestFirstLimitedDiscrepancySearchFactory<T, A, V extends Comparable<V>>
		extends StandardORGraphSearchFactory<GraphSearchWithNodeRecommenderInput<T, A>, EvaluatedSearchGraphPath<T, A, V>, T, A, V, Node<T, NodeOrderList>, A> {

	@Override
	public BestFirstLimitedDiscrepancySearch<T, A, V> getAlgorithm() {
		if (getProblemInput() == null)
			throw new IllegalArgumentException("Cannot create algorithm; problem input has not been set yet");
		return new BestFirstLimitedDiscrepancySearch<T, A, V>(getProblemInput());
	}
}
