package ai.libs.jaicore.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.SetUtil;

public class Graph<T> implements Serializable {

	private static final long serialVersionUID = 3912962578399588845L;

	private T root;
	private final Set<T> nodes = new HashSet<>();
	private final Map<T, Set<T>> successors = new HashMap<>();
	private final Map<T, Set<T>> predecessors = new HashMap<>();

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
		for (T i : toClone.nodes) {
			this.addItem(i);
		}
		for (T i : this.nodes) {
			for (T i2 : toClone.getSuccessors(i)) {
				this.addEdge(i, i2);
			}
		}
	}

	public void addItem(final T item) {
		this.nodes.add(item);
		if (this.root == null) {
			this.root = item;
		}
		if (!this.hasItem(item)) {
			throw new IllegalStateException("Just added node " + item + " does not respond positively on a call to hasItem");
		}
	}

	public void addPath(final List<T> path) {
		T parent = null;
		for (T node : path) {
			if (!this.hasItem(node)) {
				this.addItem(node);
			}
			if (parent != null && !this.getPredecessors(node).contains(parent)) {
				this.addEdge(parent, node);
			}
			parent = node;
		}
	}

	public Set<T> getItems() {
		return Collections.unmodifiableSet(this.nodes);
	}

	public boolean hasItem(final T item) {
		return this.nodes.contains(item);
	}

	public boolean hasEdge(final T from, final T to) {
		return this.successors.containsKey(from) && this.successors.get(from).contains(to);
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
		this.successors.computeIfAbsent(from, n -> new HashSet<>()).add(to);
		this.predecessors.computeIfAbsent(to, n -> new HashSet<>()).add(from);

		/* update root if necessary */
		if (to == this.root) {
			this.root = null;
			if (this.getPredecessors(from).isEmpty()) {
				this.root = from;
			}
		}
	}

	public void removeEdge(final T from, final T to) {
		this.checkNodeExistence(from);
		this.checkNodeExistence(to);
		this.successors.get(from).remove(to);
		this.predecessors.get(to).remove(from);

		/* update root if necessary */
		if (from == this.root) {
			this.root = null;
			if (this.getPredecessors(to).isEmpty()) {
				this.root = to;
			}
		}
	}

	public Set<T> getSuccessors(final T item) {
		this.checkNodeExistence(item);
		return Collections.unmodifiableSet(this.successors.containsKey(item) ? this.successors.get(item) : new HashSet<>());
	}

	public Set<T> getPredecessors(final T item) {
		this.checkNodeExistence(item);
		return Collections.unmodifiableSet(this.predecessors.containsKey(item) ? this.predecessors.get(item) : new HashSet<>());
	}

	private void checkNodeExistence(final T item) {
		if (!this.nodes.contains(item)) {
			throw new IllegalArgumentException("Cannot perform operation on node " + item + ", which does not exist!");
		}
	}

	public final Collection<T> getSources() {
		return this.nodes.stream().filter(n -> !this.predecessors.containsKey(n) || this.predecessors.get(n).isEmpty()).collect(Collectors.toList());
	}

	public final T getRoot() {
		return this.root;
	}

	public final Collection<T> getSinks() {
		return this.nodes.stream().filter(n -> !this.successors.containsKey(n) || this.successors.get(n).isEmpty()).collect(Collectors.toList());
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.nodes == null) ? 0 : this.nodes.hashCode());
		result = prime * result + ((this.successors == null) ? 0 : this.successors.hashCode());
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
		Graph other = (Graph) obj;
		if (this.nodes == null) {
			if (other.nodes != null) {
				return false;
			}
		} else if (!this.nodes.equals(other.nodes)) {
			return false;
		}
		if (this.successors == null) {
			if (other.successors != null) {
				return false;
			}
		} else if (!this.successors.equals(other.successors)) {
			return false;
		}
		return true;
	}

	/**
	 * Creates a new line for each path in the graph where the prefix common to the previous line is omitted.
	 * The order is obtained by BFS.
	 **/
	public String getLineBasedStringRepresentation() {
		return this.getLineBasedStringRepresentation(1);
	}

	public String getLineBasedStringRepresentation(final int offset) {
		StringBuilder sb = new StringBuilder();
		for (T root : this.getSources()) {
			sb.append(this.getLineBasedStringRepresentation(root, offset, new ArrayList<>()));
		}
		return sb.toString();
	}

	private String getLineBasedStringRepresentation(final T node, final int outerOffset, final List<Boolean> childrenOffset) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < outerOffset; i++) {
			sb.append("\t");
		}
		for (boolean lastChild : childrenOffset) {
			sb.append(lastChild ? " " : "|");
			sb.append("      ");
		}
		if (!childrenOffset.isEmpty()) {
			sb.append("+----- ");
		}
		sb.append(node.toString());
		Collection<T> successors = this.getSuccessors(node);
		int n = successors.size();
		int i = 1;
		for (T successor : successors) {
			sb.append("\n");
			List<Boolean> childrenOffsetCopy = new ArrayList<>(childrenOffset);
			childrenOffsetCopy.add(i++ == n);
			sb.append(this.getLineBasedStringRepresentation(successor, outerOffset, childrenOffsetCopy));
		}
		return sb.toString();
	}

	public boolean isGraphSane() {

		/* check that all nodes are contained */
		boolean allNodesContained = this.nodes.stream().allMatch(this::hasItem);
		if (!allNodesContained) {
			assert allNodesContained : "Not every node n in the node map have positive responses for a call of hasItem(n)";
		return false;
		}

		/* check that all successors are contained */
		boolean allSuccessorsContained = this.nodes.stream().allMatch(n -> this.getSuccessors(n).stream().allMatch(this::hasItem));
		if (!allSuccessorsContained) {
			assert allSuccessorsContained : "There is a node in the graph such that not every successor n of it has a positive response for a call of hasItem(n)";
		return false;
		}

		/* check that all predecessors are contained */
		return true;
	}
}