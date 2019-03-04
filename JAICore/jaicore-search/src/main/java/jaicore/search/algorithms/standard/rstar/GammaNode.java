package jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import jaicore.search.model.travesaltree.Node;

/**
 * Node wrapper for usage in R*.
 *
 * Every node is equipped with path to its backpointer i.e. parent, the g-value and
 * the AVOID label.
 *
 * @param <T> problem state type
 * @param <V> internal label type (in R* its RStarK)
 */
@SuppressWarnings("serial")
public class GammaNode<T> extends Node<T, RStarK> {

	public double g = Double.MAX_VALUE;
	public boolean avoid = false;

	/**
	 * List of all predecessors for this node.
	 * Initialized here and then filled throughout R* processes.
	 */
	private Collection<GammaNode<T>> predecessors = new ArrayList<>();

	/**
	 * Maps from each successor s_ to the lowest cost for path(this, s_).
	 * This is either a heuristic estimate or the actual known lowest cost.
	 */
	protected HashMap<GammaNode<T>, Double> c_low = new HashMap<>();

	/**
	 * Constructor.
	 * Ignores parent node because we use the backpointer attribute.
	 *
	 * @param point
	 */
	public GammaNode(final T point) {
		super(null, point);
	}

	@Override
	public GammaNode<T> getParent() {
		return (GammaNode<T>) super.getParent();
	}

	public boolean addPredecessor(final GammaNode<T> n) {
		return this.predecessors.add(n);
	}

	public Collection<GammaNode<T>> getPredecessors() {
		return this.predecessors;
	}
}
