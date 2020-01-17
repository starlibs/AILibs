package ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.HashSet;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

/**
 * Internal representation of nodes to maintain pareto front.
 */
public class ParetoNode<T, A, V extends Comparable<V>> {

	private final BackPointerPath<T, A, V> node;

	/* Number of creation of this pareto node. */
	private final HashSet<ParetoNode<T, A, V>> dominates;
	private final HashSet<ParetoNode<T, A, V>> dominatedBy;

	public ParetoNode(final BackPointerPath<T, A, V> node) {
		this.node = node;
		this.dominates = new HashSet<>();
		this.dominatedBy = new HashSet<>();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{" + this.node.getHead() + "] dominated by {");
		for (ParetoNode<T, A, V> p : this.dominatedBy) {
			sb.append(p.node.getHead() + ",");
		}
		sb.append("} dominates { ");
		for (ParetoNode<T, A, V> p : this.dominates) {
			sb.append(p.node.getHead() + ",");
		}
		sb.append("}");
		return sb.toString();
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
