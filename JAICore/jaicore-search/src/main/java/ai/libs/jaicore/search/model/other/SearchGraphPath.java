package ai.libs.jaicore.search.model.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPath;

public class SearchGraphPath<N, A> implements IPath<N, A> {
	private final List<N> nodes;
	private final List<A> edges;
	private final Map<String, Object> annotations;

	public SearchGraphPath(final IPath<N, A> path) {
		this (path.getNodes(), path.getArcs(), (path instanceof SearchGraphPath) ? ((SearchGraphPath) path).annotations : new HashMap<>());
	}

	public SearchGraphPath(final List<N> nodes) {
		this(nodes, new ArrayList<>(), new HashMap<>());
	}

	public SearchGraphPath(final List<N> nodes, final List<A> edges) {
		this(nodes, edges, new HashMap<>());
	}

	public SearchGraphPath(final List<N> nodes, final List<A> edges, final Map<String, Object> annotations) {
		super();
		this.nodes = nodes;
		this.edges = edges;
		this.annotations = annotations;
	}

	@Override
	public List<N> getNodes() {
		return Collections.unmodifiableList(this.nodes);
	}

	@Override
	public List<A> getArcs() {
		return this.edges != null ? Collections.unmodifiableList(this.edges) : null;
	}

	public Map<String, Object> getAnnotations() {
		return this.annotations;
	}

	public void setAnnotation(final String key, final Object value) {
		this.annotations.put(key, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.annotations == null) ? 0 : this.annotations.hashCode());
		result = prime * result + ((this.edges == null) ? 0 : this.edges.hashCode());
		result = prime * result + ((this.nodes == null) ? 0 : this.nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SearchGraphPath other = (SearchGraphPath) obj;
		if (this.annotations == null) {
			if (other.annotations != null) {
				return false;
			}
		} else if (!this.annotations.equals(other.annotations)) {
			return false;
		}
		if (this.edges == null) {
			if (other.edges != null) {
				return false;
			}
		} else if (!this.edges.equals(other.edges)) {
			return false;
		}
		if (this.nodes == null) {
			if (other.nodes != null) {
				return false;
			}
		} else if (!this.nodes.equals(other.nodes)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SearchGraphPath [nodes=" + this.nodes + ", edges=" + this.edges + ", annotations=" + this.annotations + "]";
	}

	@Override
	public N getRoot() {
		return this.nodes.get(0);
	}

	@Override
	public N getHead() {
		return this.nodes.get(this.nodes.size() - 1);
	}

	@Override
	public IPath<N, A> getPathToParentOfHead() {
		return new SearchGraphPath<>(this.nodes.subList(0, this.nodes.size()), this.edges.subList(0, this.edges.size()));
	}
}
