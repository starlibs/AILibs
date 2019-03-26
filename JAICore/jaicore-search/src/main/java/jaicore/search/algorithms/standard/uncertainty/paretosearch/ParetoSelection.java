package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.*;

import jaicore.search.model.travesaltree.Node;

/**
 * Open collection pareto front implementation.
 * 
 * @param <T> internal label of node
 * @param <V> external label of node
 */
public class ParetoSelection<T, V extends Comparable<V>> implements Queue<Node<T, V>> {

	/* Contains all open nodes. */
	private final LinkedList<Node<T, V>> open;

	/* Contains all maximal open nodes. */
	private final Queue<Node<T, V>> pareto;

	/* Node counter. */
	private int n = 0;

	/**
	 * Constructor.
	 * 
	 * @param pareto Pareto set implementation.
	 */
	public ParetoSelection(Queue<Node<T, V>> pareto) {
		open = new LinkedList<>();
		this.pareto = pareto;
	}

	/**
	 * Tests if p dominates q.
	 * 
	 * @param p
	 * @param q
	 * @return true if p dominates q. False, otherwise.
	 */
	private boolean dominates(Node<T, V> p, Node<T, V> q) {
		// Get f and u values of nodes
		V p_f = (V) p.getAnnotation("f");
		double p_u = (double) p.getAnnotation("uncertainty");
		V q_f = (V) q.getAnnotation("f");
		double q_u = (double) q.getAnnotation("uncertainty");

		// p dominates q <=> (q.f < p.f AND q.u <= p.u) OR (q.f <= p.f AND q.u < p.u)
		if (((p_f.compareTo(q_f) < 0) && (p_u <= q_u)) || ((p_f.compareTo(q_f) <= 0) && (p_u < q_u))) {
			return true;
		}

		return false;
	}

	/**
	 * Tests if p is maximal.
	 * 
	 * @param n
	 * @return
	 */
	private boolean isMaximal(Node n) {
		return ((HashSet<Node<T, V>>) n.getAnnotation("dominatedBy")).size() == 0;
	}

	/**
	 * Adds a node to the open list and, if its not dominated by any other point
	 * also to the pareto front.
	 * 
	 * @param n
	 * @return
	 */
	@Override
	public boolean add(Node<T, V> n) {
		assert n.getInternalLabel() != null : "Cannot add nodes with value NULL to OPEN!";

		// Initialize annotations necessary for pareto queue.
		n.setAnnotation("n", this.n++);
		n.setAnnotation("dominates", new HashSet<Node<T, V>>());
		n.setAnnotation("dominatedBy", new HashSet<Node<T, V>>());

		for (Node<T, V> q : this.open) {
			// n dominates q
			if (this.dominates(n, q)) {
				((HashSet<Node<T, V>>) n.getAnnotation("dominates")).add(q);
				((HashSet<Node<T, V>>) q.getAnnotation("dominatedBy")).add(n);

				// Remove q from pareto front if its now dominated i.e. not maximal anymore.
				if (!this.isMaximal(q)) {
					this.pareto.remove(q);
				}
			}
			// n dominated by q
			if (this.dominates(q, n)) {
				((HashSet<Node<T, V>>) n.getAnnotation("dominates")).add(q);
				((HashSet<Node<T, V>>) q.getAnnotation("dominatedBy")).add(n);
			}
		}

		// If n is not dominated by any other point, add it to pareto front.
		if (isMaximal(n)) {
			this.pareto.add(n);
		}

		return open.add(n);
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
		return this.pareto.isEmpty() ? null : this.pareto.peek();
	}

	/**
	 * Removes an Node from
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public boolean remove(Object o) {
		if (o instanceof Node) {
			Node<T, V> n = (Node<T, V>) o;
			// Remove all associations of n.
			for (Node<T, V> q : (HashSet<Node<T, V>>) n.getAnnotation("dominates")) {
				((HashSet<Node<T, V>>) q.getAnnotation("dominatedBy")).remove(n);
				// Add q to pareto if its now no longer dominated by any other point.
				if (this.isMaximal(q)) {
					this.pareto.add(q);
				}
			}
			for (Node<T, V> q : (HashSet<Node<T, V>>) n.getAnnotation("dominatedBy")) {
				((HashSet<Node<T, V>>) q.getAnnotation("dominates")).remove(n); // TODO: Is this even necessary?
			}
			// Remove n from Pareto set and Open list.
			pareto.remove(n);
			return open.remove(n);
		} else {
			return false;
		}
	}

	public String toString() {
		String s = "OPEN LIST: \n";
		for (Node p : this.open) {
			s += p.toString() + "\n";
		}
		s += "PARETO = [";
		for (Node<T, V> p : this.pareto) {
			s += p.getPoint() + ", ";
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
		Node<T, V> n = peek();
		remove(n);
		return n;
	}

	@Override
	public Node<T, V> remove() {
		return poll();
	}
}
