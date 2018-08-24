package jaicore.search.structure.core;

import jaicore.graphvisualizer.gui.HeatValueSupplier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node<T, V extends Comparable<V>> implements Serializable, Comparable<Node<T, V>>{
	private static final long serialVersionUID = -7608088086719059550L;
	private final T externalLabel;
	private boolean goal;
	private Node<T, V> parent;
	private final Map<String, Object> annotations = new HashMap<>(); // for nodes effectively examined

	public Node(Node<T, V> parent, T point) {
		super();
		this.parent = parent;
		this.externalLabel = point;
	}

	public Node<T, V> getParent() {
		return parent;
	}

	public T getPoint() {
		return externalLabel;
	}

	@SuppressWarnings("unchecked")
	public V getInternalLabel() {
		return (V)annotations.get("f");
	}
	
	public void setParent(Node<T,V> newParent) {
		this.parent = newParent;
	}
	
	public void setInternalLabel(V internalLabel) {
		this.setAnnotation("f", internalLabel);
	}
	
	public void setAnnotation(String annotationName, Object annotationValue) {
		this.annotations.put(annotationName, annotationValue);
	}
	
	public Object getAnnotation(String annotationName) {
		return this.annotations.get(annotationName);
	}
	
	public Map<String,Object> getAnnotations() {
		return this.annotations;
	}

	public boolean isGoal() {
		return goal;
	}

	public void setGoal(boolean goal) {
		this.goal = goal;
	}

	public List<Node<T, V>> path() {
		List<Node<T, V>> path = new ArrayList<>();
		Node<T, V> current = this;
		while (current != null) {
			path.add(0, current);
			current = current.parent;
		}
		return path;
	}

	public List<T> externalPath() {
		List<T> path = new ArrayList<>();
		Node<T, V> current = this;
		while (current != null) {
			path.add(0, current.externalLabel);
			current = current.parent;
		}
		return path;
	}

	@Override
	public int compareTo(Node<T, V> o) {
		return this.getInternalLabel().compareTo(o.getInternalLabel());
	}

	public String getString() {
		String s = "Node [ref=";
		s += this.toString();
		s += ", externalLabel=";
		s += externalLabel;
		s += ", goal";
		s += goal;
		s += ", parentRef=";
		if (parent != null)
			s += parent.toString();
		else
			s += "null";
		s += ", annotations=";
		s += annotations;
		s += "]";

//		return "Node [ref=" + this.toString() + ", externalLabel=" + externalLabel + ", goal=" + goal + ", parentRef=" + parent.toString() + ", annotations=" + annotations + "]";
		return s;
	}

	@Override
	public String toString() {
		return "Node [externalLabel=" + externalLabel + ", goal=" + goal + ", annotations=" + annotations + "]";
	}
}