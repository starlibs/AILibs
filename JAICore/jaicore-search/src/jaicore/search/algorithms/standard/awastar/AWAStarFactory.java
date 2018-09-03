package jaicore.search.algorithms.standard.awastar;

import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;

public class AWAStarFactory<I extends GeneralEvaluatedTraversalTree<N, A, V>, N, A, V extends Comparable<V>>
		extends StandardORGraphSearchFactory<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V, Node<N, V>, A> {

	@Override
	public AwaStarSearch<I, N, A, V> getAlgorithm() {
		return new AwaStarSearch<>(getProblemInput());
	}

}
