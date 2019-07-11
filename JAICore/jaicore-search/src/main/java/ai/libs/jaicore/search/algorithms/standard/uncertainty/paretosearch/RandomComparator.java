package ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;
import java.util.Random;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class RandomComparator<T, A, V extends Comparable<V>> implements Comparator<BackPointerPath<T, A, V>> {

	private final Random random = new Random(System.currentTimeMillis());

	/**
	 * Randomly outputs a negative or positive integer. (Never zero).
	 *
	 * @param first
	 * @param second
	 * @return negative iff first.n < second.n, 0 iff first.n == second.n, positive iff first.n > second.n
	 */
	@Override
	public int compare(final BackPointerPath<T, A, V> first, final BackPointerPath<T, A, V> second) {
		int r = this.random.nextInt(2); // This is either 0 or 1.
		if (r == 0) {
			return -1;
		} else {
			return 1;
		}
	}

}
