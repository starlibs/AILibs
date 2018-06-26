package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.OpenCollection;

/**
 * Open collection pareto front implementation.
 * @param <T> internal label of node
 * @param <V> external label of node
 */
public class ParetoSelection <T, V extends Comparable<V>> implements OpenCollection<Node<T, V>> {

	/* Contains all open nodes. */
	private final LinkedList<ParetoNode> open;

	/* Contains all maximal open nodes. */
	private final Queue<Node<T, V>> pareto;

	/**
	 * Internal representation of nodes to maintain pareto front.
	 */
	private class ParetoNode {
		final Node<T, V> node;
		final HashSet<ParetoNode> dominates;
		final HashSet<ParetoNode> dominatedBy;

		public ParetoNode(Node<T, V> node) {
			this.node = node;
			this.dominates = new HashSet<>();
			this.dominatedBy = new HashSet<>();
		}

		public String toString() {
			String s = "{" + node.getPoint() + "] dominated by {";
			for (ParetoNode p : dominatedBy) {
				s += p.node.getPoint() + ",";
			}
			s += "} dominates { ";
			for (ParetoNode p : dominates) {
				s += p.node.getPoint() + ",";
			}
			s += "}";
			return s;
		}
	}

	/**
	 * Constructor.
	 * @param pareto Pareto set implementation.
	 */
	public ParetoSelection(Queue<Node<T,V>> pareto) {
		open = new LinkedList<>();
		this.pareto = pareto;
	}

	/**
	 * Tests if p dominates q.
	 * @param p
	 * @param q
	 * @return true if p dominates q. False, otherwise.
	 */
	private boolean dominates(Node<T, V> p, Node<T, V> q) {
		// Get f and u values of nodes
		V 		p_f = (V) p.getAnnotation("f");
		double 	p_u = (double) p.getAnnotation("uncertainty");
		V 		q_f = (V) q.getAnnotation("f");
		double 	q_u = (double) q.getAnnotation("uncertainty");

		// p dominates q <=> (q.f < p.f AND q.u <= p.u) OR (q.f <= p.f AND q.u < p.u)
		if (((p_f.compareTo(q_f) < 0) && (p_u <= q_u)) || ((p_f.compareTo(q_f) <= 0) && (p_u < q_u))) {
			return true;
		}

		return false;
	}

	/**
	 * Tests if p is maximal.
	 * @param n
	 * @return
	 */
	private boolean isMaximal(ParetoNode n) {
		return n.dominatedBy.size() == 0;
	}

	/**
	 * Adds a node to the open list and, if its not dominated by any other point
	 * also to the pareto front.
	 * @param n
	 * @return
	 */
	@Override
	public boolean add(Node<T, V> n) {
		ParetoNode p = new ParetoNode(n);
		for (ParetoNode q : this.open) {
			// p dominates q
			if (this.dominates(p.node, q.node)) {
				p.dominates.add(q);
				q.dominatedBy.add(p);
				// Remove q from pareto front if its now dominated,
				if (q.dominatedBy.size() != 0) {
					this.pareto.remove(q.node);
				}
			}
			// p dominated by q
			if (this.dominates(q.node, p.node)) {
				p.dominatedBy.add(q);
				q.dominates.add(p);
			}
		}

		// If p is not dominated by any other point, add it to pareto front.
		if (isMaximal(p)) {
			this.pareto.add(p.node);
		}

		return open.add(p);
	}

	@Override
	public boolean addAll(Collection<? extends Node<T, V>> c) {
		boolean changed = false;
		for (Node<T, V> p : c) {
			changed |= this.add(p);
		}
		return changed;
	}


//	/**
//	 * Calculates the set of nodes that a given node dominates.
//	 * And adds this node to the dominatedBy set of the dominated nodes.
//	 * @param p
//	 * @return Set of nodes that p dominates.
//	 */
//	private void calcDominatesSet(ParetoNode p) {
//		V p_f = (V) p.node.getAnnotation("f");
//		double p_uncertainty = (double) p.node.getAnnotation("uncertainty");
//
//		for (ParetoNode q : this.open) {
//			V q_f = (V) q.node.getAnnotation("f");
//			double q_uncertainty = (double) q.node.getAnnotation("uncertainty");
//
//			// p dominates q <=> (q.f < p.f AND q.u <= p.u) OR (q.f <= p.f AND q.u < p.u)
//			if (((q_f.compareTo(p_f) < 0) && (q_uncertainty <= p_uncertainty)) || ((q_f.compareTo(p_f) <= 0) && (q_uncertainty < p_uncertainty))) {
//				p.dominates.add(q);
//				q.dominatedBy.add(p);
//				// Remove q from pareto front if its now dominated,
//				if (q.dominatedBy.size() == 1) {
//					this.pareto.remove(q);
//				}
//			}
//		}
//	}
//
//	/**
//	 * Calculates the set of nodes that a given node is dominated by.
//	 * And adds this node to the dominates set of the dominating nodes.
//	 * @param p
//	 * @return
//	 */
//	private void calcDominatedBySet(ParetoNode p) {
//		V p_f = (V) p.node.getAnnotation("f");
//		double p_uncertainty = (double) p.node.getAnnotation("uncertainty");
//
//		for (ParetoNode q : this.open) {
//			V q_f = (V) q.node.getAnnotation("f");
//			double q_uncertainty = (double) q.node.getAnnotation("uncertainty");
//
//			// p dominated by q <=> (p.f < q.f AND p.u <= q.u) OR (p.f <= q.f AND p.u < q.u)
//			if (((p_f.compareTo(q_f) < 0) && (p_uncertainty <= q_uncertainty)) || ((p_f.compareTo(q_f) <= 0) && (p_uncertainty < q_uncertainty))) {
//				p.dominatedBy.add(q);
//				q.dominates.add(p);
//			}
//		}
//	}

	@Override
	public void clear() {
		open.clear();
	}

	@Override
	public boolean contains(Object o) {
		return open.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return open.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return open.isEmpty();
	}

	@Override
	public Iterator<Node<T, V>> iterator() {
		return pareto.iterator();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return open.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return open.retainAll(c);
	}

	@Override
	public int size() {
		return open.size();
	}

	@Override
	public Object[] toArray() {
		return open.toArray();
	}

	@Override
	public <X> X[] toArray(X[] a) {
		return open.toArray(a);
	}

	/**
	 * Return a node from pareto front.
	 */
	@Override
	public Node<T, V> peek() {
		return this.pareto.peek();
	}

	/**
	 * Removes an Node from
	 * @param o
	 * @return
	 */
	@Override
	public boolean remove(Object o) {
		if (o instanceof Node) {
			// Find corresponding pareto node p.
			ParetoNode p = null;
			for (ParetoNode q : this.open) {
//				String n_hex =  Integer.toHexString(o.hashCode());
//				String q_hex =  Integer.toHexString(q.node.hashCode());
//				System.out.println("Check. " + q.node + " with " + o + ", " + n_hex + " == " + q_hex);
				if (q.node == o)
//					System.out.println("Check true. " + q.node + " with " + o + ", " + n_hex + " == " + q_hex);
					p = q;
			}
			if (p == null) {
				throw new IllegalArgumentException("Node to remove is not part of the open list (" + o + ").");
			}
			// Remove all associations of p.
			for (ParetoNode q : p.dominates) {
				q.dominatedBy.remove(p);
				// Add q to pareto if its now no longer dominated by any other point.
				if (this.isMaximal(q)) {
					this.pareto.add(q.node);
				}
			}
			for (ParetoNode q : p.dominatedBy) {
				q.dominates.remove(p); // TODO: Is this even necessary?
			}
			this.pareto.remove(o);
			return this.open.remove(p);
		} else {
			return false;
		}
	}

	public String toString() {
		String s = "OPEN LIST: \n";
		for (ParetoNode p : this.open) {
			s += p.toString() + "\n";
		}
		s += "PARETO = [";
		for (Node<T,V> n : this.pareto) {
			s += n.getPoint() + ", ";
		}
		s += "]";
		return s;
	}

}
