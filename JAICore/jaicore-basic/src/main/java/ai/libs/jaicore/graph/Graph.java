package ai.libs.jaicore.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.SetUtil;

public class Graph<T> {
	private T root;
	private final Map<T, Set<T>> successors = new HashMap<>();
	private final Map<T, Set<T>> predecessors = new HashMap<>();
	private boolean useBackPointers = true;
	private boolean useForwardPointers = true;

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
		for (T i : toClone.getItems()) {
			for (T i2 : toClone.getSuccessors(i)) {
				this.addEdge(i, i2);
			}
		}
	}

	public void addItem(final T item) {
		if (this.getItems().contains(item)) {
			throw new IllegalArgumentException("Cannot add node " + item + " to graph since such a node exists already. Current nodes: " + this.getItems().stream().map(e -> "\n\t" + e).collect(Collectors.joining()));
		}
		if (this.useForwardPointers) {
			this.successors.put(item, new HashSet<>());
		}
		if (this.useBackPointers) {
			this.predecessors.put(item, new HashSet<>());
		}
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
		return Collections.unmodifiableSet(this.successors.keySet());
	}

	public boolean hasItem(final T item) {
		if (this.useForwardPointers) {
			return this.successors.containsKey(item);
		}
		if (this.useBackPointers) {
			return this.predecessors.containsKey(item);
		}
		throw new IllegalStateException("Graph must use forward pointers and/or backward pointers.");
	}

	public boolean hasEdge(final T from, final T to) {
		return this.successors.containsKey(from) && this.successors.get(from).contains(to);
	}

	public boolean hasPath(final List<T> nodes) {
		T last = null;
		for (T current : nodes) {
			if (last != null && !this.hasEdge(last, current)) {
				return false;
			}
			last = current;
		}
		return true;
	}

	public void removeItem(final T item) {
		if (this.useForwardPointers) {
			this.successors.remove(item); // remove successors of this node
			this.successors.forEach((k, v) -> v.remove(item)); // erase the node from successor lists
		}
		if (this.useBackPointers) {
			this.predecessors.remove(item); // remove predecessors of this node
			this.predecessors.forEach((k, v) -> v.remove(item)); // erase the node from predecessor lists
		}
	}

	public void addEdge(final T from, final T to) {
		if (!this.hasItem(from)) {
			this.addItem(from);
		}
		if (!this.hasItem(to)) {
			this.addItem(to);
		}
		if (this.useForwardPointers) {
			this.successors.get(from).add(to);
		}
		if (this.useBackPointers) {
			this.predecessors.computeIfAbsent(to, n -> new HashSet<>()).add(from);
		}

		/* update root if necessary */
		if (to == this.root) {
			this.root = null;
			if (this.predecessors.get(from).isEmpty()) {
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
		if (!this.useForwardPointers) {
			throw new UnsupportedOperationException();
		}

		if (!this.successors.containsKey(item)) {
			throw new IllegalStateException("No predecessor map defined for node " + item);
		}
		return Collections.unmodifiableSet(this.successors.get(item));
	}

	public Set<T> getPredecessors(final T item) {
		if (!this.useBackPointers) {
			throw new UnsupportedOperationException();
		}
		if (!this.predecessors.containsKey(item)) {
			throw new IllegalStateException("No predecessor map defined for node " + item);
		}
		return Collections.unmodifiableSet(this.predecessors.get(item));
	}

	private void checkNodeExistence(final T item) {
		if (!this.hasItem(item)) {
			throw new IllegalArgumentException("Cannot perform operation on node " + item + ", which does not exist!");
		}
	}

	public final Collection<T> getSources() {
		Collection<T> sources;
		if (this.useBackPointers) {
			sources = new ArrayList<>();
			for (Entry<T, Set<T>> parentRelations : this.predecessors.entrySet()) {
				if (parentRelations.getValue().isEmpty()) {
					sources.add(parentRelations.getKey());
				}
			}
		}
		else if (this.useForwardPointers) {
			sources = new HashSet<>(this.successors.keySet());
			for (Entry<T, Set<T>> childRelations : this.successors.entrySet()) {
				sources.removeAll(childRelations.getValue());
			}
		}
		else {
			throw new UnsupportedOperationException("Neither forward edges nor backward edges are contained.");
		}
		return sources;
	}

	public final T getRoot() {
		return this.root;
	}

	public final Collection<T> getSinks() {
		Collection<T> sinks;
		if (this.useForwardPointers) {
			sinks = new ArrayList<>();
			for (Entry<T, Set<T>> childRelations : this.successors.entrySet()) {
				if (childRelations.getValue().isEmpty()) {
					sinks.add(childRelations.getKey());
				}
			}
		}
		else if (this.useBackPointers){
			sinks = new HashSet<>(this.predecessors.keySet());
			for (Entry<T, Set<T>> parentRelations : this.predecessors.entrySet()) {
				sinks.removeAll(parentRelations.getValue());
			}
		}
		else {
			throw new UnsupportedOperationException("Neither forward edges nor backward edges are contained.");
		}
		return sinks;
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
		return this.useForwardPointers ? this.successors.isEmpty() : this.predecessors.isEmpty();
	}

	/**
	 * Creates a new line for each path in the graph where the prefix common to the previous line is omitted.
	 * The order is obtained by BFS.
	 **/
	public String getLineBasedStringRepresentation() {
		return this.getLineBasedStringRepresentation(1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.predecessors.hashCode();
		result = prime * result + this.successors.hashCode();
		result = prime * result + (this.useBackPointers ? 1231 : 1237);
		result = prime * result + (this.useForwardPointers ? 1231 : 1237);
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
		if (!this.predecessors.equals(other.predecessors)) {
			return false;
		}
		if (!this.successors.equals(other.successors)) {
			return false;
		}
		if (this.useBackPointers != other.useBackPointers) {
			return false;
		}
		return this.useForwardPointers == other.useForwardPointers;
	}

	public String getLineBasedStringRepresentation(final int offset) {
		StringBuilder sb = new StringBuilder();
		for (T source : this.getSources()) {
			sb.append(this.getLineBasedStringRepresentation(source, offset, new ArrayList<>()));
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
		Collection<T> successorsOfThisNode = this.getSuccessors(node);
		int n = successorsOfThisNode.size();
		int i = 1;
		for (T successor : successorsOfThisNode) {
			sb.append("\n");
			List<Boolean> childrenOffsetCopy = new ArrayList<>(childrenOffset);
			childrenOffsetCopy.add(i++ == n);
			sb.append(this.getLineBasedStringRepresentation(successor, outerOffset, childrenOffsetCopy));
		}
		return sb.toString();
	}

	public boolean isGraphSane() {

		/* check that all nodes are contained */
		boolean allNodesContained = this.getItems().stream().allMatch(this::hasItem);
		if (!allNodesContained) {
			assert allNodesContained : "Not every node n in the node map have positive responses for a call of hasItem(n)";
		return false;
		}

		/* check that all successors are contained */
		boolean allSuccessorsContained = this.getItems().stream().allMatch(n -> this.getSuccessors(n).stream().allMatch(this::hasItem));
		if (!allSuccessorsContained) {
			assert allSuccessorsContained : "There is a node in the graph such that not every successor n of it has a positive response for a call of hasItem(n)";
		return false;
		}

		/* check that all predecessors are contained */
		return true;
	}

	public boolean isUseBackPointers() {
		return this.useBackPointers;
	}

	public void setUseBackPointers(final boolean useBackPointers) {
		this.useBackPointers = useBackPointers;
		if (!useBackPointers) {
			this.predecessors.clear();
		}
	}

	public boolean isUseForwardPointers() {
		return this.useForwardPointers;
	}

	public void setUseForwardPointers(final boolean useForwardPointers) {
		this.useForwardPointers = useForwardPointers;
		if (!useForwardPointers) {
			this.successors.clear();
		}
	}
}