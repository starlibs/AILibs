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

	public Graph(T node) {
		this();
		this.addItem(node);
	}

	public Graph(Collection<T> nodes) {
		this();
		for (T node : nodes)
			this.addItem(node);
	}

	public Graph(Graph<T> toClone) {
		this();
		System.out.println("Starting clone computation");
		for (T i : toClone.nodes.keySet())
			this.addItem(i);
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

	public void addItem(T item) {
		Node n = new Node();
		n.t = item;
		this.nodes.put(item, n);
	}

	public Set<T> getItems() {
		return nodes.keySet();
	}

	public void removeItem(T item) {
		for (T successor : getSuccessors(item))
			removeEdge(item, successor);
		for (T predecessor : getPredecessors(item))
			removeEdge(predecessor, item);
		this.nodes.remove(item);
	}

	public void addEdge(T from, T to) {
		checkNodeExistence(from);
		checkNodeExistence(to);
		Node nodeFrom = this.nodes.get(from);
		Node nodeTo = this.nodes.get(to);
		nodeFrom.successors.add(nodeTo);
		nodeTo.predecessors.add(nodeFrom);
		edges.add(new Pair<>(from, to));
	}

	public void removeEdge(T from, T to) {
		checkNodeExistence(from);
		checkNodeExistence(to);
		Node nodeFrom = this.nodes.get(from);
		Node nodeTo = this.nodes.get(to);
		nodeFrom.successors.remove(nodeTo);
		nodeTo.predecessors.remove(nodeFrom);
		edges.remove(new Pair<>(from, to));
	}

	public Set<T> getSuccessors(T item) {
		checkNodeExistence(item);
		Set<T> successors = new HashSet<>();
		for (Node n : nodes.get(item).successors)
			successors.add(n.t);
		return successors;
	}

	public Set<T> getPredecessors(T item) {
		checkNodeExistence(item);
		Set<T> predecessors = new HashSet<>();
		for (Node n : nodes.get(item).predecessors)
			predecessors.add(n.t);
		return predecessors;
	}

	private void checkNodeExistence(T item) {
		if (!this.nodes.keySet().contains(item))
			throw new IllegalArgumentException("Cannot perform operation on node " + item + ", which does not exit!");
	}

	public final Collection<T> getSources() {
		return nodes.keySet().stream().filter(n -> nodes.get(n).predecessors.isEmpty()).collect(Collectors.toList());
	}

	public final T getRoot() {
		Collection<T> sources = getSources();
		if (sources.isEmpty())
			throw new NoSuchElementException("The graph is empty, so it has no root");
		else if (sources.size() > 1)
			throw new NoSuchElementException("The graph has several sources, so no unique root can be returned");
		return sources.iterator().next();
	}

	public final Collection<T> getSinks() {
		return nodes.keySet().stream().filter(n -> nodes.get(n).successors.isEmpty()).collect(Collectors.toList());
	}

	public final void addGraph(Graph<T> g) {
		for (T t : SetUtil.difference(g.getItems(), getItems())) {
			this.addItem(t);
		}
		for (T t1 : g.getItems()) {
			for (T t2 : g.getSuccessors(t1)) {
				addEdge(t1, t2);
			}
		}
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}
	
	public Set<Pair<T, T>> getEdges() {
		return edges;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodes.keySet() == null) ? 0 : nodes.keySet().hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Graph<T> other = (Graph<T>) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		}

		for (T t : nodes.keySet()) {
			if (!other.nodes.containsKey(t))
				return false;
			Set<T> predecessors = getPredecessors(t);
			Set<T> predecessorsOther = other.getPredecessors(t);
			if (!predecessors.equals(predecessorsOther))
				return false;
			Set<T> successors = getSuccessors(t);
			Set<T> successorsOther = other.getSuccessors(t);
			if (!successors.equals(successorsOther))
				return false;
		}
		return true;
	}
	
	/**
	 * Creates a new line for each path in the graph where the prefix common to the previous line is omitted.
	 * The order is obtained by BFS.
	 **/
	public String getLineBasedStringRepresentation() {
		StringBuilder sb = new StringBuilder();
		for (T root : getSources()) {
			sb.append(root);
			sb.append(getLineBasedStringRepresentation(root, 1));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private String getLineBasedStringRepresentation(T node, int offset) {
		StringBuilder sb = new StringBuilder();
		for (T successor : getSuccessors(node)) {
			sb.append(" -> " + successor);
			sb.append(getLineBasedStringRepresentation(successor, offset + 1));
			sb.append("\n");
			for (int i = 0; i < offset; i++)
				sb.append("\t");
		}
		return sb.toString();
	}
	
}