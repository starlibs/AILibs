package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.lang.Comparable;
import java.util.HashSet;

import jaicore.search.model.travesaltree.Node;

/**
 * Internal representation of nodes to maintain pareto front.
 */
public class ParetoNode<T, V extends Comparable<V>> {


    final Node<T, V> node;
    /* Number of creation of this pareto node. */
    final int n;
    final HashSet<ParetoNode<T,V>> dominates;
    final HashSet<ParetoNode<T,V>> dominatedBy;


    public ParetoNode(Node<T, V> node, int n) {
        this.node = node;
        assert n >= 0 : "n has to be non-negative";
        this.n = n;
        this.dominates = new HashSet<>();
        this.dominatedBy = new HashSet<>();
    }

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
}
