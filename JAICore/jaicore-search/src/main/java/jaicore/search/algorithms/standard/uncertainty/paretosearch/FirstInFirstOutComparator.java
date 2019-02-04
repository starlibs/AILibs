package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;

public class FirstInFirstOutComparator<T, V extends Comparable<V>> implements Comparator<ParetoNode<T, V>> {

    /**
     * Compares two Pareto nodes on time of insertion (n). FIFO behaviour.
     * @param first
     * @param second
     * @return negative iff first.n < second.n, 0 iff fist.n == second.n, positive iff first.n > second.n
     */
    public int compare(ParetoNode<T, V> first, ParetoNode<T, V> second) {
        return first.n - second.n;
    }

}
