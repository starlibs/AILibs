package jaicore.graph;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
    private final T root;
    private final List<Tree<T>> children = new ArrayList<Tree<T>>();

    public Tree(T rootData) {
        root = rootData;
    }
    
    public void addChild(T child) {
    	children.add(new Tree<>(child));
    }
    
    public void removeChild(T child) {
    	children.removeIf(c -> c.root.equals(child));
    }

	public T getRoot() {
		return root;
	}

	public List<Tree<T>> getChildren() {
		return children;
	}
}
