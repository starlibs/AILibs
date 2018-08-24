package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import jaicore.search.structure.core.GraphGenerator;

public interface IGraphDependentNodeEvaluator<T, A, V extends Comparable<V>> extends INodeEvaluator<T, V> {
	public void setGenerator(GraphGenerator<T, A> generator);
}
