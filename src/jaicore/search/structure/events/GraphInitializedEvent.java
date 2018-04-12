package jaicore.search.structure.events;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GraphInitializedEvent<T> {
	
	
	
	private T root;

	public GraphInitializedEvent(T root) {
		super();
		this.root = root;
	}

	public T getRoot() {
		return root;
	}

	public void setRoot(T root) {
		this.root = root;
	}


}
