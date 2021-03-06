package ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.ENodeAnnotation;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

/**
 * Open collection pareto front implementation.
 *
 * @param <T>
 *            internal label of node
 * @param <V>
 *            external label of node
 */
public class ParetoSelection<T, A, V extends Comparable<V>> implements Queue<BackPointerPath<T, A, V>> {

	private static final String UNCERTAINTY = ENodeAnnotation.F_UNCERTAINTY.name();
	private static final String DOMINATES = "dominates";
	private static final String DOMINATED_BY = "dominatedBy";

	/* Contains all open nodes. */
	private final LinkedList<BackPointerPath<T, A, V>> open;

	/* Contains all maximal open nodes. */
	private final Queue<BackPointerPath<T, A, V>> pareto;

	/* Node counter. */
	private int n = 0;

	/**
	 * Constructor.
	 *
	 * @param pareto
	 *            Pareto set implementation.
	 */
	public ParetoSelection(final Queue<BackPointerPath<T, A, V>> pareto) {
		this.open = new LinkedList<>();
		this.pareto = pareto;
	}

	/**
	 * Tests if p dominates q.
	 *
	 * @param p
	 * @param q
	 * @return true if p dominates q. False, otherwise.
	 */
	@SuppressWarnings("unchecked")
	private boolean dominates(final BackPointerPath<T, A, V> p, final BackPointerPath<T, A, V> q) {
		if (!p.getAnnotations().containsKey(UNCERTAINTY)) {
			throw new IllegalArgumentException("Node " + p + " has no uncertainty information.");
		}
		if (!q.getAnnotations().containsKey(UNCERTAINTY)) {
			throw new IllegalArgumentException("Node " + q + " has no uncertainty information.");
		}
		// Get f and u values of nodes
		V p_f = (V) p.getAnnotation("f");
		double p_u = (double) p.getAnnotation(UNCERTAINTY);
		V q_f = (V) q.getAnnotation("f");
		double q_u = (double) q.getAnnotation(UNCERTAINTY);

		// p dominates q <=> (q.f < p.f AND q.u <= p.u) OR (q.f <= p.f AND q.u < p.u)
		return ((p_f.compareTo(q_f) < 0) && (p_u <= q_u)) || ((p_f.compareTo(q_f) <= 0) && (p_u < q_u));
	}

	/**
	 * Tests if p is maximal.
	 *
	 * @param n
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean isMaximal(final BackPointerPath<T, A, V> n) {
		return ((HashSet<BackPointerPath<T, A, V>>) n.getAnnotation(DOMINATED_BY)).size() == 0;
	}

	/**
	 * Adds a node to the open list and, if its not dominated by any other point
	 * also to the pareto front.
	 *
	 * @param n
	 * @return
	 */
	@Override
	public boolean add(final BackPointerPath<T, A, V> n) {
		if (n.getScore() == null) {
			throw new IllegalArgumentException("Cannot add nodes with value NULL to OPEN!");
		}

		// Initialize annotations necessary for pareto queue.
		n.setAnnotation("n", this.n++);
		n.setAnnotation(DOMINATES, new HashSet<BackPointerPath<T, A, V>>());
		n.setAnnotation(DOMINATED_BY, new HashSet<BackPointerPath<T, A, V>>());

		for (BackPointerPath<T, A, V> q : this.open) {
			// n dominates q
			if (this.dominates(n, q)) {
				((HashSet<BackPointerPath<T, A, V>>) n.getAnnotation(DOMINATES)).add(q);
				((HashSet<BackPointerPath<T, A, V>>) q.getAnnotation(DOMINATED_BY)).add(n);

				// Remove q from pareto front if its now dominated i.e. not maximal anymore.
				if (!this.isMaximal(q)) {
					this.pareto.remove(q);
				}
			}
			// n dominated by q
			if (this.dominates(q, n)) {
				((HashSet<BackPointerPath<T, A, V>>) n.getAnnotation(DOMINATES)).add(q);
				((HashSet<BackPointerPath<T, A, V>>) q.getAnnotation(DOMINATED_BY)).add(n);
			}
		}

		// If n is not dominated by any other point, add it to pareto front.
		if (this.isMaximal(n)) {
			this.pareto.add(n);
		}

		return this.open.add(n);
	}

	@Override
	public boolean addAll(final Collection<? extends BackPointerPath<T, A, V>> c) {
		boolean changed = false;
		for (BackPointerPath<T, A, V> p : c) {
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
		return this.open.contains(o);
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
	public Iterator<BackPointerPath<T, A, V>> iterator() {
		return this.pareto.iterator();
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
	public BackPointerPath<T, A, V> peek() {
		return this.pareto.isEmpty() ? null : this.pareto.peek();
	}

	/**
	 * Removes an Node from
	 *
	 * @param o
	 * @return
	 */
	@Override
	public boolean remove(final Object o) {
		if (o instanceof BackPointerPath) {
			BackPointerPath<T, A, V> node = (BackPointerPath<T, A, V>) o;
			// Remove all associations of n.
			for (BackPointerPath<T, A, V> q : (HashSet<BackPointerPath<T, A, V>>) node.getAnnotation(DOMINATES)) {
				((HashSet<BackPointerPath<T, A, V>>) q.getAnnotation(DOMINATED_BY)).remove(node);
				// Add q to pareto if its now no longer dominated by any other point.
				if (this.isMaximal(q)) {
					this.pareto.add(q);
				}
			}
			for (BackPointerPath<T, A, V> q : (HashSet<BackPointerPath<T, A, V>>) node.getAnnotation(DOMINATED_BY)) {
				((HashSet<BackPointerPath<T, A, V>>) q.getAnnotation(DOMINATES)).remove(node); // TODO: Is this even necessary?
			}
			// Remove n from Pareto set and Open list.
			this.pareto.remove(node);
			return this.open.remove(node);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OPEN LIST: \n");
		for (BackPointerPath p : this.open) {
			sb.append(p.toString());
			sb.append("\n");
		}
		sb.append("PARETO = [");
		for (BackPointerPath<T, A, V> p : this.pareto) {
			sb.append(p.getHead());
			sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public BackPointerPath<T, A, V> element() {
		return this.peek();
	}

	@Override
	public boolean offer(final BackPointerPath<T, A, V> arg0) {
		return this.add(arg0);
	}

	@Override
	public BackPointerPath<T, A, V> poll() {
		BackPointerPath<T, A, V> node = this.peek();
		this.remove(node);
		return node;
	}

	@Override
	public BackPointerPath<T, A, V> remove() {
		return this.poll();
	}
}
