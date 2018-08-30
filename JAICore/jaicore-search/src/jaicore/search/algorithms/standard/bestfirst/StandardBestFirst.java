package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class StandardBestFirst<N,A,V extends Comparable<V>> extends BestFirst<GeneralEvaluatedTraversalTree<N, A, V>, N, A, V> {

	public StandardBestFirst(GeneralEvaluatedTraversalTree<N, A, V> problem) {
		super(problem);
	}
}
