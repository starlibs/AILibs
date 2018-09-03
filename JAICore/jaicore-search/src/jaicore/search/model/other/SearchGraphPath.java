package jaicore.search.model.other;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchGraphPath<N, A> {
	private final List<N> nodes;
	private final List<A> edges;
	private final Map<String, Object> annotations;

	public SearchGraphPath(List<N> nodes, List<A> edges) {
		this(nodes, edges, new HashMap<>());
	}
	
	public SearchGraphPath(List<N> nodes, List<A> edges, Map<String, Object> annotations) {
		super();
		this.nodes = nodes;
		this.edges = edges;
		this.annotations = annotations;
	}

	public List<N> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public List<A> getEdges() {
		return edges != null ? Collections.unmodifiableList(edges) : null;
	}

	public Map<String, Object> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotation(String key, Object value) {
		this.annotations.put(key, value);
	}
}
