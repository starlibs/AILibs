package ai.libs.jaicore.basic;

import java.util.Collection;

public class RobustnessUtil {

	private RobustnessUtil() {
		// intentionally left blank.
	}

	public static void sameSizeOrDie(final Collection<?> collectionA, final Collection<?> collectionB) {
		if (collectionA.size() != collectionB.size()) {
			throw new IllegalArgumentException("The collections must be of the same size");
		}
	}

	public static void sameLengthOrDie(final String stringA, final String stringB) {
		if (stringA.length() != stringB.length()) {
			throw new IllegalArgumentException("The two strings must be of the same length");
		}
	}

	public static void sameLengthOrDie(final double[] arrayA, final double[] arrayB) {
		arraysOfSameLength(arrayA.length != arrayB.length);
	}

	public static void sameLengthOrDie(final int[] arrayA, final int[] arrayB) {
		arraysOfSameLength(arrayA.length != arrayB.length);
	}

	public static void sameLengthOrDie(final boolean[] arrayA, final boolean[] arrayB) {
		arraysOfSameLength(arrayA.length != arrayB.length);
	}

	public static void sameLengthOrDie(final Object[] arrayA, final Object[] arrayB) {
		arraysOfSameLength(arrayA.length != arrayB.length);
	}

	private static void arraysOfSameLength(final boolean sameLength) {
		if (sameLength) {
			throw new IllegalArgumentException("The two arrays must be of the same length");
		}
	}

}
