package jaicore.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.order.SetUtil;

@SuppressWarnings("serial")
public class LabeledGraph<T, L> extends Graph<T> {

	private static class Edge<T> {
		private T from, to;

		public Edge(T from, T to) {
			super();
			this.from = from;
			this.to = to;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
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
			Edge<?> other = (Edge<?>) obj;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Edge [from=" + from + ", to=" + to + "]";
		}
	}

	private final Map<Edge<T>, L> labels = new HashMap<>();
	
	public LabeledGraph() {
		super();
	}
	
	public LabeledGraph(LabeledGraph<T, L> graph) {
		super();
		for (T i : graph.getItems())
			this.addItem(i);
		for (T i : getItems()) {
			try {
				for (T i2 : graph.getSuccessors(i)) {
					this.addEdge(i, i2, graph.getEdgeLabel(i, i2));
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void addEdge(T from, T to) {
		addEdge(from, to, null);
	}

	public void addEdge(T from, T to, L label) {
		super.addEdge(from, to);
		if (label == null)
			throw new IllegalArgumentException("No null-labels allowed!");
		this.labels.put(new Edge<>(from, to), label);
	}
	
	public final void addGraph(LabeledGraph<T, L> g) {
		for (T t : SetUtil.difference(g.getItems(), getItems())) {
			this.addItem(t);
		}
		for (T t1 : g.getItems()) {
			for (T t2 : g.getSuccessors(t1)) {
				addEdge(t1, t2, g.getEdgeLabel(t1, t2));
			}
		}
	}

	public L getEdgeLabel(T from, T to) {
		Edge<T> e = new Edge<>(from, to);
		if (!labels.containsKey(e)) {
			List<T> targets = new ArrayList<>();
			for (Edge<T> e2 : labels.keySet()) {
				if (e2.from.equals(from)) {
					targets.add(e2.to);
					if (e2.to.equals(to)) {
						if (!labels.containsKey(e2))
							throw new IllegalStateException("The edge " + e2 + " with hashCode " + e2.hashCode()
									+ " is contained in the labeling but cannot be accessed anymore using new edge object with hash value " + e.hashCode() + "!");
						System.err.println("WHATT??" + e2.hashCode());
					}
				}
			}
			throw new IllegalArgumentException("No label for the edge from " + from + " (" + from.hashCode() + ") to " + to + " (" + to.hashCode()
					+ ") is available! List of targets of " + from + ": " + targets);
		}
		return this.labels.get(e);
	}
}
