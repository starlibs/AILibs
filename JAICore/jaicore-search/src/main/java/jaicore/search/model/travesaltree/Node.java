package jaicore.search.model.travesaltree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;

public class Node<T, V extends Comparable<V>> {
	private final T externalLabel;
	private boolean goal;
	protected Node<T, V> parent;
	private final Map<String, Object> annotations = new HashMap<>(); // for nodes effectively examined

	public Node(final Node<T, V> parent, final T point) {
		super();
		this.parent = parent;
		this.externalLabel = point;
	}

	public Node<T, V> getParent() {
		return this.parent;
	}

	public T getPoint() {
		return this.externalLabel;
	}

	@SuppressWarnings("unchecked")
	public V getInternalLabel() {
		return (V) this.annotations.get("f");
	}

	public void setParent(final Node<T, V> newParent) {
		this.parent = newParent;
	}

	public void setInternalLabel(final V internalLabel) {
		this.setAnnotation("f", internalLabel);
	}

	public void setAnnotation(final String annotationName, final Object annotationValue) {
		this.annotations.put(annotationName, annotationValue);
	}

	public Object getAnnotation(final String annotationName) {
		return this.annotations.get(annotationName);
	}

	public Map<String, Object> getAnnotations() {
		return this.annotations;
	}

	public boolean isGoal() {
		return this.goal;
	}

	public void setGoal(final boolean goal) {
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

	public String getString() {
		String s = "Node [ref=";
		s += this.toString();
		s += ", externalLabel=";
		s += this.externalLabel;
		s += ", goal";
		s += this.goal;
		s += ", parentRef=";
		if (this.parent != null) {
			s += this.parent.toString();
		} else {
			s += "null";
		}
		s += ", annotations=";
		s += this.annotations;
		s += "]";
		return s;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("externalLabel", this.externalLabel);
		fields.put("goal", this.goal);
		fields.put("annotations", this.annotations);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}