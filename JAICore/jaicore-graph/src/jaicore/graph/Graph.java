package jaicore.graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil;
import jaicore.basic.sets.SetUtil.Pair;

public class Graph<T> implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -4773369406288261706L;

	private class Node implements Serializable {
		private static final long serialVersionUID = 4994522466549020297L;
		T t = null;
		Set<Node> successors = new HashSet<>();
		Set<Node> predecessors = new HashSet<>();
	}

	private final Map<T, Node> nodes = new HashMap<>();
	private final Set<Pair<T, T>> edges = new HashSet<>();

	public Graph() {
	}

	public Graph(final T node) {
		this();
		this.addItem(node);
	}

	public Graph(final Collection<T> nodes) {
		this();
		for (T node : nodes) {
			this.addItem(node);
		}
	}

	public Graph(final Graph<T> toClone) {
		this();
		System.out.println("Starting clone computation");
		for (T i : toClone.nodes.keySet()) {
			this.addItem(i);
		}
		for (T i : this.nodes.keySet()) {
			try {
				for (T i2 : toClone.getSuccessors(i)) {
					this.addEdge(i, i2);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.println("Finished clone computation");
	}

	public void addItem(final T item) {
		Node n = new Node();
		n.t = item;
		this.nodes.put(item, n);
	}

	public Set<T> getItems() {
		return this.nodes.keySet();
	}

	public void removeItem(final T item) {
		for (T successor : this.getSuccessors(item)) {
			this.removeEdge(item, successor);
		}
		for (T predecessor : this.getPredecessors(item)) {
			this.removeEdge(predecessor, item);
		}
		this.nodes.remove(item);
	}

	public void addEdge(final T from, final T to) {
		this.checkNodeExistence(from);
		this.checkNodeExistence(to);
		Node nodeFrom = this.nodes.get(from);
		Node nodeTo = this.nodes.get(to);
		nodeFrom.successors.add(nodeTo);
		nodeTo.predecessors.add(nodeFrom);
		this.edges.add(new Pair<>(from, to));
	}

	public void removeEdge(final T from, final T to) {
		this.checkNodeExistence(from);
		this.checkNodeExistence(to);
		Node nodeFrom = this.nodes.get(from);
		Node nodeTo = this.nodes.get(to);
		nodeFrom.successors.remove(nodeTo);
		nodeTo.predecessors.remove(nodeFrom);
		this.edges.remove(new Pair<>(from, to));
	}

	public Set<T> getSuccessors(final T item) {
		this.checkNodeExistence(item);
		Set<T> successors = new HashSet<>();
		for (Node n : this.nodes.get(item).successors) {
			successors.add(n.t);
		}
		return successors;
	}

	public Set<T> getPredecessors(final T item) {
		this.checkNodeExistence(item);
		Set<T> predecessors = new HashSet<>();
		for (Node n : this.nodes.get(item).predecessors) {
			predecessors.add(n.t);
		}
		return predecessors;
	}

	private void checkNodeExistence(final T item) {
		if (!this.nodes.keySet().contains(item)) {
			throw new IllegalArgumentException("Cannot perform operation on node " + item + ", which does not exit!");
		}
	}

	public final Collection<T> getSources() {
		return this.nodes.keySet().stream().filter(n -> this.nodes.get(n).predecessors.isEmpty()).collect(Collectors.toList());
	}

	public final T getRoot() {
		Collection<T> sources = this.getSources();
		if (sources.isEmpty()) {
			throw new NoSuchElementException("The graph is empty, so it has no root");
		} else if (sources.size() > 1) {
			throw new NoSuchElementException("The graph has several sources, so no unique root can be returned");
		}
		return sources.iterator().next();
	}

	public final Collection<T> getSinks() {
		return this.nodes.keySet().stream().filter(n -> this.nodes.get(n).successors.isEmpty()).collect(Collectors.toList());
	}

	public final void addGraph(final Graph<T> g) {
		for (T t : SetUtil.difference(g.getItems(), this.getItems())) {
			this.addItem(t);
		}
		for (T t1 : g.getItems()) {
			for (T t2 : g.getSuccessors(t1)) {
				this.addEdge(t1, t2);
			}
		}
	}

	public boolean isEmpty() {
		return this.nodes.isEmpty();
	}

	public Set<Pair<T, T>> getEdges() {
		return this.edges;
	}

	public int size() {
		return this.nodes.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.nodes.keySet() == null) ? 0 : this.nodes.keySet().hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
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
		Graph<T> other = (Graph<T>) obj;
		if (this.nodes == null) {
			if (other.nodes != null) {
				return false;
			}
		}

		for (T t : this.nodes.keySet()) {
			if (!other.nodes.containsKey(t)) {
				return false;
			}
			Set<T> predecessors = this.getPredecessors(t);
			Set<T> predecessorsOther = other.getPredecessors(t);
			if (!predecessors.equals(predecessorsOther)) {
				return false;
			}
			Set<T> successors = this.getSuccessors(t);
			Set<T> successorsOther = other.getSuccessors(t);
			if (!successors.equals(successorsOther)) {
				return false;
			}
		}
		return true;
	}
}