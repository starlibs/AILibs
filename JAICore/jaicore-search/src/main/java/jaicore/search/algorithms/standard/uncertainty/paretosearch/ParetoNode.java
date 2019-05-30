package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.HashSet;

import jaicore.search.model.travesaltree.Node;

/**
 * Internal representation of nodes to maintain pareto front.
 */
public class ParetoNode<T, V extends Comparable<V>> {

	private final Node<T, V> node;

	/* Number of creation of this pareto node. */
	private final HashSet<ParetoNode<T,V>> dominates;
	private final HashSet<ParetoNode<T,V>> dominatedBy;


	public ParetoNode(final Node<T, V> node) {
		this.node = node;
		this.dominates = new HashSet<>();
		this.dominatedBy = new HashSet<>();
	}

	@Override
	public String toString() {
		String s = "{" + this.node.getPoint() + "] dominated by {";
		for (ParetoNode<T,V> p : this.dominatedBy) {
			s += p.node.getPoint() + ",";
		}
		s += "} dominates { ";
		for (ParetoNode<T,V> p : this.dominates) {
			s += p.node.getPoint() + ",";
		}
		s += "}";
		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.node == null) ? 0 : this.node.hashCode());
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
		ParetoNode other = (ParetoNode) obj;
		if (this.node == null) {
			if (other.node != null) {
				return false;
			}
		} else if (!this.node.equals(other.node)) {
			return false;
		}
		return true;
	}
}
