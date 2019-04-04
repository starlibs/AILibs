package jaicore.search.model.probleminputs;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IUncertaintyAnnotatingNodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;

public class UncertainlyEvaluatedTraversalTree<N, A, V extends Comparable<V>> extends GeneralEvaluatedTraversalTree<N, A, V> {
	
	public UncertainlyEvaluatedTraversalTree(GraphGenerator<N, A> graphGenerator, IUncertaintyAnnotatingNodeEvaluator<N, V> nodeEvaluator) {
		super(graphGenerator, nodeEvaluator);
	}

}
