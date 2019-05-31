package jaicore.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.basic.sets.SetUtil;
import jaicore.basic.sets.SetUtil.Pair;

public class LabeledGraph<T, L> extends Graph<T> {

	private static class Edge<T> {
		private T from;
		private T to;

		public Edge(final T from, final T to) {
			super();
			this.from = from;
			this.to = to;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.from == null) ? 0 : this.from.hashCode());
			result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
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
			Edge<?> other = (Edge<?>) obj;
			if (this.from == null) {
				if (other.from != null) {
					return false;
				}
			} else if (!this.from.equals(other.from)) {
				return false;
			}
			if (this.to == null) {
				if (other.to != null) {
					return false;
				}
			} else if (!this.to.equals(other.to)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "Edge [from=" + this.from + ", to=" + this.to + "]";
		}
	}

	private final Map<Edge<T>, L> labels = new HashMap<>();

	public LabeledGraph() {
		super();
	}

	public LabeledGraph(final LabeledGraph<T, L> graph) {
		super();
		for (T i : graph.getItems()) {
			this.addItem(i);
		}
		for (T i : this.getItems()) {
			for (T i2 : graph.getSuccessors(i)) {
				this.addEdge(i, i2, graph.getEdgeLabel(i, i2));
			}
		}
	}

	@Override
	public void addEdge(final T from, final T to) {
		this.addEdge(from, to, null);
	}

	public void addEdge(final T from, final T to, final L label) {
		super.addEdge(from, to);
		if (label == null) {
			throw new IllegalArgumentException("No null-labels allowed!");
		}
		this.labels.put(new Edge<>(from, to), label);
	}

	public final void addGraph(final LabeledGraph<T, L> g) {
		for (T t : SetUtil.difference(g.getItems(), this.getItems())) {
			this.addItem(t);
		}
		for (T t1 : g.getItems()) {
			for (T t2 : g.getSuccessors(t1)) {
				this.addEdge(t1, t2, g.getEdgeLabel(t1, t2));
			}
		}
	}

	public L getEdgeLabel(final Pair<T, T> edge) {
		return this.getEdgeLabel(edge.getX(), edge.getY());
	}

	public L getEdgeLabel(final T from, final T to) {
		Edge<T> e = new Edge<>(from, to);
		if (!this.labels.containsKey(e)) {
			List<T> targets = new ArrayList<>();
			for (Edge<T> e2 : this.labels.keySet()) {
				if (e2.from.equals(from)) {
					targets.add(e2.to);
					if (e2.to.equals(to)) {
						if (!this.labels.containsKey(e2)) {
							throw new IllegalStateException("The edge " + e2 + " with hashCode " + e2.hashCode() + " is contained in the labeling but cannot be accessed anymore using new edge object with hash value " + e.hashCode() + "!");
						}
						assert false : "This line should never be reached!";
					}
				}
			}
			throw new IllegalArgumentException("No label for the edge from " + from + " (" + from.hashCode() + ") to " + to + " (" + to.hashCode() + ") is available! List of targets of " + from + ": " + targets);
		}
		return this.labels.get(e);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.labels.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		LabeledGraph other = (LabeledGraph) obj;
		return this.labels.equals(other.labels);
	}
}
