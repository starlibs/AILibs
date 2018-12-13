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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchGraphPath other = (SearchGraphPath) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SearchGraphPath [nodes=" + nodes + ", edges=" + edges + ", annotations=" + annotations + "]";
	}
}
