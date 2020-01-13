package ai.libs.jaicore.search.model.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.graph.ReadOnlyPathAccessor;

public class SearchGraphPath<N, A> implements ILabeledPath<N, A> {
	private final List<N> nodes;
	private final List<A> edges;
	private final Map<String, Object> annotations;

	public SearchGraphPath(final ILabeledPath<N, A> path) {
		this (path.getNodes(), path.getArcs(), (path instanceof SearchGraphPath) ? ((SearchGraphPath) path).annotations : new HashMap<>());
	}

	public SearchGraphPath(final ILabeledPath<N, A> pathA, final ILabeledPath<N, A> pathB, final A link) {
		this.nodes = new ArrayList<>();
		this.nodes.addAll(pathA.getNodes());
		this.nodes.addAll(pathB.getNodes());
		this.edges = new ArrayList<>();
		this.edges.addAll(pathA.getArcs());
		this.edges.add(link);
		this.edges.addAll(pathB.getArcs());
		this.annotations = new HashMap<>();
	}

	public SearchGraphPath(final ILabeledPath<N, A> pathA, final N attachedNode, final A link) {
		this.nodes = new ArrayList<>();
		this.nodes.addAll(pathA.getNodes());
		this.nodes.add(attachedNode);
		this.edges = new ArrayList<>();
		this.edges.addAll(pathA.getArcs());
		this.edges.add(link);
		this.annotations = new HashMap<>();
	}

	public SearchGraphPath(final N node) {
		this(new ArrayList<>(Collections.singletonList(node)), new ArrayList<>(), new HashMap<>());
	}

	public SearchGraphPath(final List<N> nodes, final List<A> edges) {
		this(nodes, edges, new HashMap<>());
	}

	public SearchGraphPath(final List<N> nodes, final List<A> edges, final Map<String, Object> annotations) {
		super();
		if (nodes.isEmpty()) {
			throw new IllegalArgumentException("List of nodes of a path must not be empty!");
		}
		if (edges == null || nodes.size() != edges.size() + 1) {
			throw new IllegalArgumentException("Number of edges must be exactly one less than the one of nodes! Number of nodes: " + nodes.size() + ". Edges: " + (edges != null ? edges.size() : null));
		}
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
		return new HashCodeBuilder().append(this.nodes).append(this.edges).append(this.annotations).toHashCode();
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
		return new EqualsBuilder().append(this.nodes, other.nodes).append(this.edges, other.edges).isEquals();
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
	public SearchGraphPath<N, A> getPathToParentOfHead() {
		if (this.nodes.isEmpty()) {
			throw new UnsupportedOperationException("This is an empty path!");
		}
		if (this.isPoint()) {
			throw new UnsupportedOperationException("Root has no head!");
		}
		return new SearchGraphPath<>(this.nodes.subList(0, this.nodes.size() - 1), this.edges.subList(0, this.edges.size() - 1));
	}

	@Override
	public boolean isPoint() {
		return this.nodes.size() == 1;
	}

	@Override
	public int getNumberOfNodes() {
		return this.nodes.size();
	}

	@Override
	public ILabeledPath<N, A> getPathFromChildOfRoot() {
		return new SearchGraphPath<>(this.nodes.subList(1, this.nodes.size()), this.edges != null ? this.edges.subList(1, this.edges.size()) : null);
	}

	@Override
	public A getInArc(final N node) {
		return this.edges.get(this.nodes.indexOf(node) - 1);
	}

	@Override
	public A getOutArc(final N node) {
		return this.edges.get(this.nodes.indexOf(node));
	}

	@Override
	public boolean containsNode(final N node) {
		return this.nodes.contains(node);
	}

	@Override
	public ILabeledPath<N, A> getUnmodifiableAccessor() {
		return new ReadOnlyPathAccessor<>(this);
	}

	@Override
	public N getParentOfHead() {
		return this.nodes.get(this.nodes.size() - 2);
	}

	@Override
	public void extend(final N newHead, final A arcToNewHead) {
		this.nodes.add(newHead);
		this.edges.add(arcToNewHead);
	}

	@Override
	public void cutHead() {
		if (this.isPoint()) {
			throw new NoSuchElementException("The path consists only of one point, which cannot be removed.");
		}
		this.nodes.remove(this.nodes.size() - 1);
		this.edges.remove(this.edges.size() - 1);
	}
}
