package ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;
import java.util.Random;

import ai.libs.jaicore.search.model.travesaltree.Node;

public class RandomComparator<T, V extends Comparable<V>> implements Comparator<Node<T, V>> {

	/**
	 * Randomly outputs a negative or positive integer. (Never zero).
	 * @param first
	 * @param second
	 * @return negative iff first.n < second.n, 0 iff first.n == second.n, positive iff first.n > second.n
	 */
	@Override
	public int compare(final Node<T, V> first, final Node<T, V> second) {
		Random random = new Random();
		int r = random.nextInt(2); // This is either 0 or 1.
		if (r == 0) {
			return -1;
		} else {
			return 1;
		}
	}

}
