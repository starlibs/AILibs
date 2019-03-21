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

	/**
	 * Simple Manhattan distance (sum of the absolute differences between the
	 * vectors' elements) implementation for arrays of Integer.
	 * 
	 * @param A
	 *            First argument vector
	 * @param B
	 *            Second argument vector
	 * @return Returns the Manhattan distance of the two given vectors
	 */
	public static double intManhattanDistance(final int[] A, final int[] B) {
		double result = 0;
		for (int j = 0; j < A.length; j++) {
			result += Math.abs(A[j] - B[j]);
		}
		return result;
	}

	/**
	 * Function calculating the mean of the interval [t1, t2 (inclusive)] of the
	 * given <code>vector</code>.
	 * 
	 * @param vector
	 *            Vector which is used for the calculation
	 * @param t1
	 *            Interval start
	 * @param t2
	 *            Interval end (inclusive)
	 * @return Returns the mean of the vector's interval [t1, t2 (inclusive)]
	 */
	public static double mean(final double[] vector, final int t1, final int t2) {
		checkIntervalParameters(vector, t1, t2);

		double result = 0;
		for (int i = t1; i <= t2; i++) {
			result += vector[i];
		}
		return result / (t2 - t1 + 1);
	}

	/**
	 * Function calculating the standard deviation of the interval [t1, t2
	 * (inclusive)] of the given <code>vector</code>.
	 * 
	 * @param vector
	 *            Vector which is used for the calculation
	 * @param t1
	 *            Interval start
	 * @param t2
	 *            Interval end (inclusive)
	 * @param useBiasCorrection
	 *            Indicator whether the bias (Bessel's) correction should be used
	 * @return Returns the standard deviation of the vector's interval [t1, t2
	 *         (inclusive)]
	 */
	public static double stddev(final double[] vector, final int t1, final int t2, final boolean useBiasCorrection) {
		checkIntervalParameters(vector, t1, t2);
		if (t1 == t2)
			return 0.0d;

		double mean = mean(vector, t1, t2);

		double result = 0;
		for (int i = t1; i <= t2; i++) {
			result += Math.pow(vector[i] - mean, 2);
		}

		return Math.sqrt(result / (double) (t2 - t1 + (useBiasCorrection ? 0 : 1)));
	}

	/**
	 * Function calculating the slope of the interval [t1, t2 (inclusive)] of the
	 * given <code>vector</code>.
	 * 
	 * @param vector
	 *            Vector which is used for the calculation
	 * @param t1
	 *            Interval start
	 * @param t2
	 *            Interval end (inclusive)
	 * @return Returns the slope of the vector's interval [t1, t2 (inclusive)]
	 */
	public static double slope(final double[] vector, final int t1, final int t2) {
		checkIntervalParameters(vector, t1, t2);

		if (t2 == t1)
			return 0d;

		double xx = 0;
		double x = 0;
		double xy = 0;
		double y = 0;

		for (int i = t1; i <= t2; i++) {
			x += i;
			y += vector[i];
			xx += i * i;
			xy += i * vector[i];
		}

		// Calculate slope
		int length = t2 - t1 + 1;
		return (length * xy - x * y) / (length * xx - x * x);
	}

	/**
	 * Checks the parameters <code>t1</code> and </code>t2</code> for validity given
	 * the <code>vector</code>
	 * 
	 * @param vector
	 *            Value vector
	 * @param t1
	 *            Interval start
	 * @param t2
	 *            Interval end (inclusive)
	 */
	private static void checkIntervalParameters(final double[] vector, final int t1, final int t2) {
		if (t1 >= vector.length || t2 >= vector.length)
			throw new IllegalArgumentException("Parameters t1 and t2 must be valid indices of the vector!");
		if (t2 < t1)
			throw new IllegalArgumentException("End index t2 of the interval must be greater equals start index t1!");
	}

	/**
	 * Calculates the index of the maximum value in the given <code>array</code>
	 * (argmax).
	 * 
	 * @param array
	 *            Array to be checked. Must not be null or empty
	 * @return Returns the index of the maximum value
	 */
	public static int argmax(final int[] array) {
		if (array == null || array.length == 0)
			throw new IllegalArgumentException("Given parameter 'array' must not be null or empty for argmax.");

		int maxValue = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > maxValue) {
				maxValue = array[i];
				index = i;
			}
		}
		return index;
	}
}
