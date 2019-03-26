package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import jaicore.search.model.travesaltree.Node;

/**
 * Open collection pareto front implementation.
 *
 * @param <T> internal label of node
 * @param <V> external label of node
 */
public class ParetoSelection<T, V extends Comparable<V>> implements Queue<Node<T, V>> {

	/* Contains all open nodes. */
	private final LinkedList<ParetoNode<T, V>> open;

	/* Contains all maximal open nodes. */
	private final Queue<ParetoNode<T, V>> pareto;

	/* Node counter. */
	private int n = 0;

	/**
	 * Constructor.
	 *
	 * @param pareto Pareto set implementation.
	 */
	public ParetoSelection(final Queue<ParetoNode<T, V>> pareto) {
		this.open = new LinkedList<>();
		this.pareto = pareto;
	}

	/**
	 * FIFO: ParetoSelection<T,V> p = new ParetoSelection(new PriorityQueue<ParetoNode<T,V>();)
	 */

	/**
	 * Tests whether p dominates q.
	 *
	 * @param p
	 * @param q
	 * @return true if p dominates q. False, otherwise.
	 */
	@SuppressWarnings("unchecked")
	private boolean dominates(final Node<T, V> p, final Node<T, V> q) {
		// Get f and u values of nodes
		V pF = (V) p.getAnnotation("f");
		double pU = (double) p.getAnnotation("uncertainty");
		V qF = (V) q.getAnnotation("f");
		double qU = (double) q.getAnnotation("uncertainty");

		// p dominates q <=> (q.f < p.f AND q.u <= p.u) OR (q.f <= p.f AND q.u < p.u)
		return (((pF.compareTo(qF) < 0) && (pU <= qU)) || ((pF.compareTo(qF) <= 0) && (pU < qU)));
	}

	/**
	 * Tests if p is maximal.
	 *
	 * @param n
	 * @return
	 */
	private boolean isMaximal(final ParetoNode<T, V> n) {
		return n.dominatedBy.size() == 0;
	}

	/**
	 * Adds a node to the open list and, if its not dominated by any other point
	 * also to the pareto front.
	 *
	 * @param node
	 * @return
	 */
	@Override
	public boolean add(final Node<T, V> node) {
		if (node.getInternalLabel() == null) {
			throw new IllegalArgumentException("Cannot add nodes with value NULL to OPEN!");
		}
		ParetoNode<T, V> p = new ParetoNode<>(node, this.n++);
		for (ParetoNode<T, V> q : this.open) {
			// p dominates q
			if (this.dominates(p.node, q.node)) {
				p.dominates.add(q);
				q.dominatedBy.add(p);

				/* Remove q from pareto front if it is now dominated i.e. not maximal anymore. */
				if (!this.isMaximal(q)) {
					this.pareto.remove(q);
				}
			}
			// p dominated by q
			if (this.dominates(q.node, p.node)) {
				p.dominatedBy.add(q);
				q.dominates.add(p);
			}
		}

		// If p is not dominated by any other point, add it to pareto front.
		if (this.isMaximal(p)) {
			this.pareto.add(p);
		}

		return this.open.add(p);
	}

	@Override
	public boolean addAll(final Collection<? extends Node<T, V>> c) {
		boolean changed = false;
		for (Node<T, V> p : c) {
			changed |= this.add(p);
		}
		return changed;
	}

	@Override
	public void clear() {
		this.open.clear();
	}

	@Override
	public boolean contains(final Object o) {
		if (!(o instanceof Node)) {
			return false;
		}
		return this.open.stream().anyMatch(pn -> pn.node == o);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return this.open.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return this.open.isEmpty();
	}

	@Override
	public Iterator<Node<T, V>> iterator() {
		// Convert ParetoNode-iterator from this.pareto to a Node<T,V>-iterator.
		ArrayList<Node<T, V>> a = new ArrayList<>();
		for (ParetoNode<T, V> p : this.pareto) {
			a.add(p.node);
		}
		return a.iterator();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return this.open.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return this.open.retainAll(c);
	}

	@Override
	public int size() {
		return this.open.size();
	}

	@Override
	public Object[] toArray() {
		return this.open.toArray();
	}

	@Override
	public <X> X[] toArray(final X[] a) {
		return this.open.toArray(a);
	}

	/**
	 * Return a node from pareto front.
	 */
	@Override
	public Node<T, V> peek() {
		return this.pareto.isEmpty() ? null : this.pareto.peek().node;
	}

	/**
	 * Removes an Node from
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean remove(final Object o) {
		if (!(o instanceof Node)) {
			return false;
		}

		/* Find corresponding pareto node p. */
		ParetoNode<T, V> p = null;
		for (ParetoNode<T, V> q : this.open) {
			if (q.node == o) {
				p = q;
			}
		}
		if (p == null) {
			throw new IllegalArgumentException("Node to remove is not part of the open list (" + o + ").");
		}

		/* Remove all associations of p. */
		for (ParetoNode<T, V> q : p.dominates) {
			q.dominatedBy.remove(p);
			// Add q to pareto if its now no longer dominated by any other point.
			if (this.isMaximal(q)) {
				this.pareto.add(q);
			}
		}
		for (ParetoNode<T, V> q : p.dominatedBy) {
			q.dominates.remove(p);
		}
		this.pareto.remove(p);
		return this.open.remove(p);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OPEN LIST: \n");
		for (ParetoNode<T, V> p : this.open) {
			sb.append(p.toString() + "\n");
		}
		sb.append("PARETO = [");
		for (ParetoNode<T, V> p : this.pareto) {
			sb.append(p.node.getPoint() + ", ");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Node<T, V> element() {
		return this.peek();
	}

	@Override
	public boolean offer(final Node<T, V> arg0) {
		return this.add(arg0);
	}

	@Override
	public Node<T, V> poll() {
		Node<T, V> node = this.peek();
		this.remove(node);
		return node;
	}

	@Override
	public Node<T, V> remove() {
		return this.poll();
	}
}
