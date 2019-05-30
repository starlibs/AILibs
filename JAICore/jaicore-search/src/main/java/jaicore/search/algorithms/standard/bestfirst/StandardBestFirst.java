package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class StandardBestFirst<N,A,V extends Comparable<V>> extends BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> {

	public StandardBestFirst(GraphSearchWithSubpathEvaluationsInput<N, A, V> problem) {
		super(problem);
	}
}
