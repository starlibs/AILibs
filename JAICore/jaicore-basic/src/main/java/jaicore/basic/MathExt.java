package jaicore.basic;

import java.util.HashSet;
import java.util.Set;

/**
 * A util class for some simple mathematical helpers.
 *
 * @author fmohr, mwever
 */
public class MathExt {

	private MathExt() {
		// prevent instantiation of this util class
	}

	/**
	 * Computes the binomial of n choose k.
	 * @param n The size of the whole set.
	 * @param k The size of the chosen subset.
	 * @return The number of all possible combinations of how to choose k elements from a set of n elements.
	 */
	public static long binomial(final int n, int k) {
		if (k > n - k) {
			k = n - k;
		}

		long b = 1;
		for (int i = 1, m = n; i <= k; i++, m--) {
			b = b * m / i;
		}
		return b;
	}

	/**
	 * Gets a list of all integers for a certain range "from" to "to" (both inclusively).
	 *
	 * @param from The lower bound (included).
	 * @param to The upper bound (included).
	 * @return The set of all integers of the specified range.
	 */
	public static Set<Integer> getIntegersFromTo(final int from, final int to) {
		Set<Integer> set = new HashSet<>();
		for (int i = from; i <= to; i++) {
			set.add(i);
		}
		return set;
	}

	/**
	 * Computes the double factorial of k, i.e. k!!.
	 *
	 * @param k The k for which to compute the double factorial for.
	 * @return The double factorial for the specified number k.
	 */
	public static int doubleFactorial(final int k) {
		if (k <= 0) {
			return 1;
		}
		return k * doubleFactorial(k - 2);
	}

	/**
	 * Rounds a double value to a certain number of decimal places.
	 * @param d The value to be rounded.
	 * @param precision The number of decimal places.
	 * @return The rounded value.
	 */
	public static double round(final double d, final int precision) {
		return (Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision));
	}
}
