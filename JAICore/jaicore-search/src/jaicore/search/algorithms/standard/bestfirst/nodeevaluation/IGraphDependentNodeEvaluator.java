package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import jaicore.search.core.interfaces.GraphGenerator;

public interface IGraphDependentNodeEvaluator<T, A, V extends Comparable<V>> extends INodeEvaluator<T, V> {
	public void setGenerator(GraphGenerator<T, A> generator);
}
