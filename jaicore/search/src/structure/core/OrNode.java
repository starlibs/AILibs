package jaicore.search.structure.core;

@SuppressWarnings("serial")
public class OrNode<T,V extends Comparable<V>> extends Node<T,V> {

	public OrNode(Node<T,V> parent, T point) {
		super(parent, point);
	}
}
