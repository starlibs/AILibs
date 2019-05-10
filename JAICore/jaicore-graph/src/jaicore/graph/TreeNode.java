package jaicore.graph;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
    private final T value;
    private final TreeNode<T> parent;
    private final List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();

    public TreeNode(T rootData) {
        this(rootData,null);
    }
    
    public TreeNode(T value, TreeNode<T> parent) {
        this.value = value;
        this.parent = parent;
    }
    
    public TreeNode<T> addChild(T child) {
    	TreeNode<T> childNode = new TreeNode<>(child, this);
    	children.add(childNode);
    	return childNode;
    }
    
    public void removeChild(T child) {
    	children.removeIf(c -> c.value.equals(child));
    }

	public T getValue() {
		return value;
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}
	
	public TreeNode<T> getRootNode() {
		return this.parent == null ? this : this.parent.getRootNode();
	}
	
	public List<T> getValuesOnPathFromRoot() {
		if (parent == null) {
			List<T> path = new ArrayList<>();
			path.add(this.value);
			return path;
		}
		List<T> valuesOnPathFromRootToParent = parent.getValuesOnPathFromRoot();
		valuesOnPathFromRootToParent.add(this.value);
		return valuesOnPathFromRootToParent;
	}
}
