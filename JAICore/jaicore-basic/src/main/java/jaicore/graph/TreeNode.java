package jaicore.graph;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
	private final T value;
	private final TreeNode<T> parent;
	private final List<TreeNode<T>> children = new ArrayList<>();

	public TreeNode(final T rootData) {
		this(rootData,null);
	}

	public TreeNode(final T value, final TreeNode<T> parent) {
		this.value = value;
		this.parent = parent;
	}

	public TreeNode<T> addChild(final T child) {
		TreeNode<T> childNode = new TreeNode<>(child, this);
		this.children.add(childNode);
		return childNode;
	}

	public void removeChild(final T child) {
		this.children.removeIf(c -> c.value.equals(child));
	}

	public T getValue() {
		return this.value;
	}

	public TreeNode<T> getParent() {
		return this.parent;
	}

	public List<TreeNode<T>> getChildren() {
		return this.children;
	}

	public TreeNode<T> getRootNode() {
		return this.parent == null ? this : this.parent.getRootNode();
	}

	public List<T> getValuesOnPathFromRoot() {
		if (this.parent == null) {
			List<T> path = new ArrayList<>();
			path.add(this.value);
			return path;
		}
		List<T> valuesOnPathFromRootToParent = this.parent.getValuesOnPathFromRoot();
		valuesOnPathFromRootToParent.add(this.value);
		return valuesOnPathFromRootToParent;
	}
}
