package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import jaicore.search.structure.core.Node;
import java.lang.Comparable;
import java.util.HashSet;

/**
 * Internal representation of nodes to maintain pareto front.
 */
public class ParetoNode<T, V extends Comparable<V>> {


    final Node<T, V> node;
    /* Number of creation of this pareto node. */
    final long n;
    final HashSet<ParetoNode> dominates;
    final HashSet<ParetoNode> dominatedBy;


    ParetoNode(Node<T, V> node, long n) {
        this.node = node;
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
