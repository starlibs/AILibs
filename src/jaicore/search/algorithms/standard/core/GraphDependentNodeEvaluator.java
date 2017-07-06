package jaicore.search.algorithms.standard.core;

import jaicore.search.structure.core.GraphGenerator;

public interface GraphDependentNodeEvaluator<T, A, V extends Comparable<V>> extends NodeEvaluator<T, V> {
	public void setGenerator(GraphGenerator<T, A> generator);
}
