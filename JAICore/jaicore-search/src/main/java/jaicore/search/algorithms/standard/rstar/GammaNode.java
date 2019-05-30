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
 * @author fischor, fmohr, mwever
 *
 * @param <T> problem state type
 * @param <V> internal label type (in R* its RStarK)
 */
public class GammaNode<T> extends Node<T, RStarK> {

	private double g = Double.MAX_VALUE;
	private boolean avoid = false;

	/**
	 * List of all predecessors for this node.
	 * Initialized here and then filled throughout R* processes.
	 */
	private Collection<GammaNode<T>> predecessors = new ArrayList<>();

	/**
	 * Maps from each successor s_ to the lowest cost for path(this, s_).
	 * This is either a heuristic estimate or the actual known lowest cost.
	 */
	protected HashMap<GammaNode<T>, Double> cLow = new HashMap<>();

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

	/**
	 * Add a predecessor to this node.
	 * @param n The predecessor to be added.
	 * @return Returns true iff the predecessor could be added successfully.
	 */
	public boolean addPredecessor(final GammaNode<T> n) {
		return this.predecessors.add(n);
	}

	/**
	 * @return The collection of all predecessors of this node.
	 */
	public Collection<GammaNode<T>> getPredecessors() {
		return this.predecessors;
	}

	/**
	 * @return The value of g.
	 */
	public double getG() {
		return this.g;
	}

	/**
	 * @param g The new value of g.
	 */
	public void setG(final double g) {
		this.g = g;
	}

	/**
	 * @return The value of the avoid flag of this node.
	 */
	public boolean getAvoid() {
		return this.avoid;
	}

	/**
	 * @param avoid The new value of the avoid flag of this node.
	 */
	public void setAvoid(final boolean avoid) {
		this.avoid = avoid;
	}
}
