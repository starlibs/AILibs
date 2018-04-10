package jaicore.search.algorithms.interfaces;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

public interface IObservableORGraphSearchFactory<T,A, V extends Comparable<V>> {
	public IObservableORGraphSearch<T, A, V> createSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> nodeEvaluator, int numberOfCPUs);
}
