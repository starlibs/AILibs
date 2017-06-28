package util.search.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node<T, V extends Comparable<V>> implements Serializable, Comparable<Node<T, V>> {
	private static final long serialVersionUID = -7608088086719059550L;
	private final T externalLabel;
	private boolean goal;
	private Node<T, V> parent;
	private V internalLabel;

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

	public V getInternalLabel() {
		return internalLabel;
	}

	public void setInternalLabel(V internalLabel) {
		this.internalLabel = internalLabel;
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
		return this.internalLabel.compareTo(o.getInternalLabel());
	}
}