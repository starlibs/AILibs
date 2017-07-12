package jaicore.search.algorithms.interfaces;

import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.Node;

public interface ObservableORGraphSearch<T,A, V extends Comparable<V>> extends ORGraphSearch<T, A, V> {
	public GraphEventBus<Node<T, V>> getEventBus();
}
