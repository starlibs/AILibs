package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import jaicore.search.model.travesaltree.Node;

/**
 * Open collection pareto front implementation.
 * @param <T> internal label of node
 * @param <V> external label of node
 */
public class ParetoSelection <T, V extends Comparable<V>> implements Queue<Node<T, V>> {

	/* Contains all open nodes. */
	private final LinkedList<ParetoNode<T,V>> open;

	/* Contains all maximal open nodes. */
	private final Queue<ParetoNode<T,V>> pareto;

	/* Node counter. */
	private int n = 0;

	/**
	 * Constructor.
	 * @param pareto Pareto set implementation.
	 */
	public ParetoSelection(Queue<ParetoNode<T,V>> pareto) {
		open = new LinkedList<>();
		this.pareto = pareto;
	}

	/**
	 * FIFO: ParetoSelection<T,V> p = new ParetoSelection(new PriorityQueue<ParetoNode<T,V>();)
	 */

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
		assert n.getInternalLabel() != null : "Cannot add nodes with value NULL to OPEN!";
		ParetoNode p = new ParetoNode(n, this.n++);
		for (ParetoNode q : this.open) {
			// p dominates q
			if (this.dominates(p.node, q.node)) {
				p.dominates.add(q);
				q.dominatedBy.add(p);
				// Remove q from pareto front if its now dominated i.e. not maximal anymore.
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
		if (isMaximal(p)) {
			this.pareto.add(p);
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
		// Convert ParetoNode-iterator from this.pareto to a Node<T,V>-iterator.
		// TODO: improve
		ArrayList<Node<T,V>> a = new ArrayList<>();
		for(ParetoNode<T,V> p : this.pareto)
			a.add(p.node);
		return a.iterator();
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
		return this.pareto.isEmpty() ? null : this.pareto.peek().node;
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
			ParetoNode<T,V> p = null;
			for (ParetoNode<T,V> q : this.open) {
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
			for (ParetoNode<T,V> q : p.dominates) {
				q.dominatedBy.remove(p);
				// Add q to pareto if its now no longer dominated by any other point.
				if (this.isMaximal(q)) {
					this.pareto.add(q);
				}
			}
			for (ParetoNode q : p.dominatedBy) {
				q.dominates.remove(p); // TODO: Is this even necessary?
			}
			this.pareto.remove(p);
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
		for (ParetoNode<T,V> p : this.pareto) {
			s += p.node.getPoint() + ", ";
		}
		s += "]";
		return s;
	}

	@Override
	public Node<T, V> element() {
		return peek();
	}

	@Override
	public boolean offer(Node<T, V> arg0) {
		return add(arg0);
	}

	@Override
	public Node<T, V> poll() {
		Node<T,V> n = peek();
		remove(n);
		return n;
	}

	@Override
	public Node<T, V> remove() {
		return poll();
	}
}
