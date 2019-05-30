package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;

import jaicore.search.model.travesaltree.Node;

public class FirstInFirstOutComparator<T, V extends Comparable<V>> implements Comparator<Node<T, V>> {

    /**
     * Compares two Pareto nodes on time of insertion (n). FIFO behaviour.
     * @param first
     * @param second
     * @return negative iff first.n < second.n, 0 iff fist.n == second.n, positive iff first.n > second.n
     */
    public int compare(Node<T, V> first, Node<T, V> second) {
    	return (int)first.getAnnotation("n") - (int)second.getAnnotation("n");
    }

}
