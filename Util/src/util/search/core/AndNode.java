package util.search.core;

@SuppressWarnings("serial")
public class AndNode<T,V extends Comparable<V>> extends Node<T,V> {

	/**
	 * 
	 */

	public AndNode(Node<T,V> parent, T point) {
		super(parent, point);
	}
}
