package jaicore.search.algorithms.standard.rstar;

import java.util.List;
import jaicore.search.structure.core.Node;

public class GammaNode<T, V extends Comparable<V>> extends Node<T, V> {

    public List<Node<T,V>> pathToBp = null;
    public double g = Double.MAX_VALUE;
    public boolean avoid = false;

    public GammaNode(GammaNode<T, V> parent, T point) {
        super(parent, point);
    }

    /**
     * Checks equaltiy to other gamma node by checking equality of its points.
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

}
