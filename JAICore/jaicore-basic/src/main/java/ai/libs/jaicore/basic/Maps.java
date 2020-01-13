package ai.libs.jaicore.basic;

import java.util.Map;

public class Maps {

	/**
	 * Forbid to create an object of ListHelper as there are only static methods allowed here.
	 */
	private Maps() {
		// intentionally do nothing
	}

	public static <K> void increaseCounterInMap(final Map<K, Integer> counterMap, final K key) {
		if (counterMap.containsKey(key)) {
			counterMap.put(key, counterMap.get(key) + 1);
		} else {
			counterMap.put(key, 1);
		}
	}

	public static <K> void increaseCounterInMap(final Map<K, Integer> counterMap, final K key, final int summand) {
		if (counterMap.containsKey(key)) {
			counterMap.put(key, counterMap.get(key) + summand);
		} else {
			counterMap.put(key, summand);
		}
	}

	public static <K> void increaseCounterInDoubleMap(final Map<K, Double> counterMap, final K key) {
		if (counterMap.containsKey(key)) {
			counterMap.put(key, counterMap.get(key) + 1.0);
		} else {
			counterMap.put(key, 1.0);
		}
	}

	public static <K> void increaseCounterInDoubleMap(final Map<K, Double> counterMap, final K key, final double summand) {
		if (counterMap.containsKey(key)) {
			counterMap.put(key, counterMap.get(key) + summand);
		} else {
			counterMap.put(key, summand);
		}
	}

}
