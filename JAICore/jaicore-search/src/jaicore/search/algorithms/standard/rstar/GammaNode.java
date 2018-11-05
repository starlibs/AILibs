package jaicore.search.algorithms.standard.rstar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
public class GammaNode<T, V extends Comparable<V>> extends Node<T, V> {


    protected GammaNode<T, V> backpointer = null;

    // public List<Node<T,V>> pathToBp = null;
    public double g = Double.MAX_VALUE;
    public boolean avoid = false;

    /**
     * List of generated successor for this node.
     * Inititialized with null here and set only once within R* process..
     */
    private Collection<GammaNode<T,V>> successors = null;  // TODO: make this immutable.

    /**
     * List of all predecessors for this node.
     * Initialized here and then filled throughout R* processes.
     */
    private Collection<GammaNode<T, V>> predecessors = new ArrayList<GammaNode<T, V>>();

    /**
     * Maps from each successor s_ to the lowest cost for path(this, s_).
     * This is either a heuristic estimate or the actual known lowest cost.
     */
    protected HashMap<GammaNode<T, V>, Double> c_low = new HashMap<>();

    /**
     * Maps from each successor s_ to a path from this to s_.
     */
    protected HashMap<GammaNode<T, V>, List<Node<T,V>>> path = new HashMap<>();

    /**
     * Constructor.
     * Ignores parent node because we use the backpointer attribute.
     *
     * @param point
     */
    public GammaNode(T point) {
        super(null, point);
    }

    /**
     * Sets the successors for this gamma node.
     * Since every node will only be expanded once i.e. the successors only will be generated and
     * therefore set once, an exception is thrown when trying to set the successors again.
     *
     * @param succ
     * @throws IllegalStateException When called more than once for this object.
     */
    public void setSuccessors(Collection<GammaNode<T,V>> succ) {
        if (successors != null) {
            throw new IllegalStateException("The successor of this Gamma node " + this + " have already been set. Setting twice is not allowed.");
        }
        successors = succ;
    }

    public boolean addPredecessor(GammaNode<T,V> n) {
        return predecessors.add(n);
    }

    public Collection<GammaNode<T,V>> getPredecessors() {
        return predecessors;
    }

    public Collection<GammaNode<T, V>> getSuccessors() {
        return successors;
    }


    @Override
    public int compareTo(Node<T, V> o) {


        if (o instanceof GammaNode) {

            Object k = getInternalLabel();
            Object k_ = o.getInternalLabel();

            if ((k instanceof RStarK) && (k_ instanceof RStarK)) {
                return ((RStarK) k).compareTo((RStarK)k_);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return super.compareTo(o);
    }

    /**
     * Checks equalitiy to other gamma node by checking equality of its points.
     * TODO: Assure that the points implement this equality in a sufficient way!
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GammaNode)) {
            return false;
        }
        return this.getPoint().equals(((GammaNode) other).getPoint());
    }

    @Override
    /**
     * Return hashCode of point.
     * (Define hashCode on point if you want to hash the Gamma node.)
     */
    public int hashCode() {
        return this.getPoint().hashCode();
    }
}
