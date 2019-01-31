package jaicore.ml.tsc.util;

import java.util.stream.DoubleStream;

/**
 * Utility class consisting of mathematical utility functions.
 * 
 * @author Julian Lienen
 *
 */
public class MathUtil {
	/**
	 * Function to calculate the sigmoid for the given value <code>z</code>.
	 * 
	 * @param z
	 *            Parameter z
	 * @return Returns the sigmoid for the parameter <code>z</code>.
	 */
	public static double sigmoid(final double z) {
		return 1 / (1 + Math.exp((-1) * z));
	}

	/**
	 * Sums the values of the given <code>array</code>.
	 * 
	 * @param array
	 *            The array to be summed
	 * @return Returns the sum of the values
	 */
	public static double sum(double[] array) {
		return DoubleStream.of(array).sum();
	}

	/**
	 * Computes the single squared Euclidean distance between two vectors.
	 * 
	 * @param vector1
	 *            First argument vector
	 * @param vector2
	 *            Second argument vector
	 * @return Returns the single squared Euclidean distance between two vectors
	 */
	public static double singleSquaredEuclideanDistance(final double[] vector1, final double[] vector2) {
		if (vector1.length != vector2.length)
			throw new IllegalArgumentException("The lengths of of both vectors must match!");

		double distance = 0;
		for (int i = 0; i < vector1.length; i++) {
			distance += Math.pow(vector1[i] - vector2[i], 2);
		}

		return distance;
	}

}
