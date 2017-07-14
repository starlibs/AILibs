package jaicore.search.algorithms.interfaces;

import jaicore.search.algorithms.standard.core.NodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

public interface IORGraphSearchFactory<T,A,V extends Comparable<V>> {
	
	public IORGraphSearch<T,A,V> getSearch(GraphGenerator<T, A> generator, NodeEvaluator<T, V> nodeEvaluator);
}
