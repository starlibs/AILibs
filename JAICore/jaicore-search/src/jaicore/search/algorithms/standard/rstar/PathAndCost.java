package jaicore.search.algorithms.standard.rstar;

import jaicore.search.structure.core.Node;

import java.util.List;

public class PathAndCost<T,V extends Comparable<V>> {

    public final List<Node<T,V>> path;
    public final double cost;

    public PathAndCost(List<Node<T,V>> path, double cost) {
        this.path = path;
        this.cost = cost;
    }

}
