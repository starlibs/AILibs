package jaicore.search.algorithms.standard.rstar;

import java.util.List;

import jaicore.search.model.travesaltree.Node;

public class PathAndCost<T,V extends Comparable<V>> {

    public final List<Node<T,V>> path;
    public final double cost;

    public PathAndCost(List<Node<T,V>> path, double cost) {
        this.path = path;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return String.format("PAC[%b, %f]", path, cost);
    }
}
