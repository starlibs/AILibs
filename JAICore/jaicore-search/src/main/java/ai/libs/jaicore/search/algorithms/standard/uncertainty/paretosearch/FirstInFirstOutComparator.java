package ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class FirstInFirstOutComparator<T, A, V extends Comparable<V>> implements Comparator<BackPointerPath<T, A, V>> {

	/**
	 * Compares two Pareto nodes on time of insertion (n). FIFO behaviour.
	 *
	 * @param first
	 * @param second
	 * @return negative iff first.n < second.n, 0 iff fist.n == second.n, positive iff first.n > second.n
	 */
	@Override
	public int compare(final BackPointerPath<T, A, V> first, final BackPointerPath<T, A, V> second) {
		return (int) first.getAnnotation("n") - (int) second.getAnnotation("n");
	}

}
