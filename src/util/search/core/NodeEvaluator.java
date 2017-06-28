package util.search.core;

public interface NodeEvaluator<T,V extends Comparable<V>> {
	public V f(Node<T,V> node);
}
