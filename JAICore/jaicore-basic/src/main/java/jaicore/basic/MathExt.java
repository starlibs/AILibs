package jaicore.basic;

import java.util.HashSet;
import java.util.Set;

public abstract class MathExt {
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

	public static Set<Integer> getIntegersFromTo(final int from, final int to) {
		Set<Integer> set = new HashSet<>();
		for (int i = from; i <= to; i++) {
			set.add(i);
		}
		return set;
	}

	public static int doubleFactorial(final int k) {
		if (k <= 0) {
			return 1;
		}
		return k * doubleFactorial(k - 2);
	}

	public static double round(final double d, final int precision) {
		return (Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision));
	}
}
