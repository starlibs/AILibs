package jaicore.search.algorithms.interfaces;

import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.Node;

public interface IObservableORGraphSearch<T,A, V extends Comparable<V>> extends IORGraphSearch<T, A, V> {
	public GraphEventBus<Node<T, V>> getEventBus();
}
