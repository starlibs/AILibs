package jaicore.ml.tsc.util;

import java.util.stream.IntStream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

/**
 * Utility class for time series operations.
 */
public class TimeSeriesUtil {

	/**
	 * Checks, whether given INDArray are valid time series.
	 * 
	 * @param array
	 * @return True, if the all arrays are valid time series.
	 */
	public static boolean isTimeSeries(INDArray... array) {
		for (INDArray a : array)
			if (a.rank() != 1)
				return false;
		return true;
	}

	/**
	 * Checks, whether given INDArrays are valid time series with a given length.
	 * 
	 * @param array
	 * @param length
	 * @return True, if the array is a valid time series of the given length. False,
	 *         otherwise.
	 */
	public static boolean isTimeSeries(int length, INDArray... array) {
		for (INDArray a : array)
			if (a.rank() != 1 && a.length() == length)
				return false;
		return true;
	}

	/**
	 * Checks, whether given array are valid time series with a given length.
	 * 
	 * @param array
	 * @param length
	 * @return True, if the array is a valid time series of the given length. False,
	 *         otherwise.
	 */
	public static boolean isTimeSeries(int length, double[]... array) {
		for (double[] a : array)
			if (a.length != length)
				return false;
		return true;
	}

	/**
	 * Checks, whether given INDArrays are valid time series. Throws an exception
	 * otherwise.
	 * 
	 * @param array
	 * @throws IllegalArgumentException
	 */
	public static void isTimeSeriesOrException(INDArray... array) throws IllegalArgumentException {
		for (INDArray a : array) {
			if (!isTimeSeries(array)) {
				String message = String.format(
						"The given INDArray is no time series. It should have rank 1, but has a rank of %d.", a.rank());
				throw new IllegalArgumentException(message);
			}
		}
	}

	/**
	 * Checks, whether given INDArrays are valid time series with a given length.
	 * Throws an exception otherwise.
	 * 
	 * @param array
	 * @param length
	 * @throws IllegalArgumentException
	 */
	public static void isTimeSeriesOrException(int length, INDArray... array) throws IllegalArgumentException {
		for (INDArray a : array) {
			if (!isTimeSeries(array)) {
				String message = String.format(
						"The given INDArray is no time series. It should have rank 1, but has a rank of %d.", a.rank());
				throw new IllegalArgumentException(message);
			}
			if (!isTimeSeries(length, a)) {
				String message = String.format("The given time series should length 7, but has a length of %d.",
						a.length());
				throw new IllegalArgumentException(message);
			}
		}
	}

	/**
	 * Checks, whether given INDArrays are valid time series with a given length.
	 * Throws an exception otherwise.
	 * 
	 * @param array
	 * @param length
	 * @throws IllegalArgumentException
	 */
	public static void isTimeSeriesOrException(int length, double[]... array) throws IllegalArgumentException {
		for (double[] a : array) {
			if (!isTimeSeries(length, a)) {
				String message = String.format("The given time series should length 7, but has a length of %d.",
						a.length);
				throw new IllegalArgumentException(message);
			}
		}
	}

	/**
	 * Checks whether multiple arrays have the same length.
	 * 
	 * @param timeSeries1
	 * @param timeSeries2
	 * @return True if the arrays have the same length. False, otherwise.
	 */
	public static boolean isSameLength(INDArray timeSeries1, INDArray... timeSeries) {
		for (INDArray t : timeSeries) {
			if (timeSeries1.length() != t.length())
				return false;
		}
		return true;
	}

	/**
	 * Checks whether multiple arrays have the same length.
	 * 
	 * @param timeSeries1
	 * @param timeSeries2
	 * @return True if the arrays have the same length. False, otherwise.
	 */
	public static boolean isSameLength(double[] timeSeries1, double[]... timeSeries) {
		for (double[] t : timeSeries) {
			if (timeSeries1.length != t.length)
				return false;
		}
		return true;
	}

	/**
	 * Checks whether multiple arrays have the same length. Throws an exception
	 * otherwise.
	 * 
	 * @param timeSeries1
	 * @param timeSeries2
	 * @throws TimeSeriesLengthException
	 */
	public static void isSameLengthOrException(INDArray timeSeries1, INDArray... timeSeries)
			throws TimeSeriesLengthException {
		for (INDArray t : timeSeries) {
			if (!isSameLength(timeSeries1, t)) {
				String message = String.format(
						"Length of the given time series are not equal: Length first time series: (%d). Length of seconds time series: (%d)",
						timeSeries1.length(), t.length());
				throw new TimeSeriesLengthException(message);
			}
		}
	}

	/**
	 * Checks whether multiple arrays have the same length. Throws an exception
	 * otherwise.
	 * 
	 * @param timeSeries1
	 * @param timeSeries2
	 * @throws TimeSeriesLengthException
	 */
	public static void isSameLengthOrException(double[] timeSeries1, double[]... timeSeries)
			throws TimeSeriesLengthException {
		for (double[] t : timeSeries) {
			if (!isSameLength(timeSeries1, t)) {
				String message = String.format(
						"Length of the given time series are not equal: Length first time series: (%d). Length of seconds time series: (%d)",
						timeSeries1.length, t.length);
				throw new TimeSeriesLengthException(message);
			}
		}
	}

	/**
	 * Creates equidistant timestamps for a time series.
	 * 
	 * @param timeSeries Time series to generate timestamps for. Let n be its
	 *                   length.
	 * @return Equidistant timestamp, i.e. {0, 1, .., n-1}.
	 */
	public static INDArray createEquidistantTimestamps(INDArray timeSeries) {
		int n = (int) timeSeries.length();
		double[] timestamps = IntStream.range(0, n).mapToDouble(t -> (double) t).toArray();
		int[] shape = { n };
		return Nd4j.create(timestamps, shape);
	}

	/**
	 * Creates equidistant timestamps for a time series.
	 * 
	 * @param timeSeries Time series to generate timestamps for. Let n be its
	 *                   length.
	 * @return Equidistant timestamp, i.e. {0, 1, .., n-1}.
	 */
	public static double[] createEquidistantTimestamps(double[] timeSeries) {
		int n = timeSeries.length;
		double[] timestamps = IntStream.range(0, n).mapToDouble(t -> (double) t).toArray();
		return timestamps;
	}

	/**
	 * Enables printing of time series.
	 * 
	 * @param timeSeries Time series to print.
	 * @return Readable string of the time series, i.e.
	 *         <code>"{1.0, 2.0, 3.0, 4.0}"</code>
	 */
	public static String toString(double[] timeSeries) {
		if (timeSeries.length == 0)
			return "{}";

		int stringLength = 2 + timeSeries.length * 3 - 1;
		StringBuilder sb = new StringBuilder(stringLength);
		sb.append("{" + timeSeries[0]);
		for (int i = 1; i < timeSeries.length; i++) {
			sb.append(", " + timeSeries[i]);
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Calculates the derivative of a timeseries as described first by Keogh and
	 * Pazzani (2001).
	 * <code>f'(n) = \frac{ f(n) - f(n-1) + /frac{f(i+1) - f(i-1)}{2} }{2}</code>
	 * 
	 * @param T
	 * @return
	 */
	public static double[] keoghDerivate(double[] T) {
		double[] derivate = new double[T.length - 2];

		for (int i = 1; i < T.length - 1; i++) {
			derivate[i - 1] = ((T[i] - T[i - 1]) + (T[i + 1] - T[i - 1]) / 2) / 2;
		}

		return derivate;
	}

	/**
	 * Calculates the derivateive of a timeseries as described first by Keogh and
	 * Pazzani (2001).
	 * <code>f'(n) = \frac{ f(n) - f(n-1) + /frac{f(i+1) - f(i-1)}{2} }{2}</code>
	 * 
	 * @param T
	 * @return
	 */
	public static double[] keoghDerivateWithBoundaries(double[] T) {
		double[] derivate = new double[T.length];

		for (int i = 1; i < T.length - 1; i++) {
			derivate[i] = ((T[i] - T[i - 1]) + (T[i + 1] - T[i - 1]) / 2) / 2;
		}

		derivate[0] = derivate[1];
		derivate[T.length - 1] = derivate[T.length - 2];

		return derivate;
	}

	/**
	 * Calclualtes f'(n) = f(n-1) - f(n)
	 * 
	 * @param T Time series.
	 * @return
	 */
	public static double[] backwardDifferenceDerivate(double[] T) {
		double[] derivate = new double[T.length - 1];

		for (int i = 1; i < T.length; i++) {
			derivate[i - 1] = T[i] - T[i - 1];
		}

		return derivate;
	}

	/**
	 * Calclualtes f'(n) = f(n-1) - f(n)
	 * 
	 * @param T Time series.
	 * @return
	 */
	public static double[] backwardDifferenceDerivateWithBoundaries(double[] T) {
		double[] derivate = new double[T.length];

		for (int i = 1; i < T.length; i++) {
			derivate[i] = T[i] - T[i - 1];
		}

		derivate[0] = derivate[1];
		return derivate;
	}

	/**
	 * f'(n) = f(n+1) - f(n)
	 * 
	 * @param T
	 * @return
	 */
	public static double[] forwardDifferenceDerivate(double[] T) {
		double[] derivate = new double[T.length - 1];

		for (int i = 0; i < T.length - 1; i++) {
			derivate[i] = T[i + 1] - T[i];
		}

		return derivate;
	}

	/**
	 * f'(n) = f(n+1) - f(n)
	 * 
	 * @param T
	 * @return
	 */
	public static double[] forwardDifferenceDerivateWithBoundaries(double[] T) {
		double[] derivate = new double[T.length];

		for (int i = 0; i < T.length - 1; i++) {
			derivate[i] = T[i + 1] - T[i];
		}

		derivate[T.length - 1] = derivate[T.length - 2];
		return derivate;
	}

	/**
	 * Calculates the derivative of a timeseries as described first by Gullo et. al
	 * (2009).
	 * 
	 * @param T
	 * @return
	 */
	public static double[] gulloDerivate(double[] T) {
		double[] derivate = new double[T.length - 1];

		for (int i = 1; i < T.length; i++) {
			derivate[i - 1] = T[i + 1] - T[i - 1] / 2;
		}

		return derivate;
	}

	/**
	 * f'(n) = \frac{f(i+1)-f(i-1)}{2}
	 * 
	 * @param T
	 * @return
	 */
	public static double[] gulloDerivateWithBoundaries(double[] T) {
		double[] derivate = new double[T.length];

		for (int i = 1; i < T.length; i++) {
			derivate[i] = T[i + 1] - T[i - 1] / 2;
		}

		derivate[0] = derivate[1];
		return derivate;
	}

	public static double sum(double[] T) {
		double sum = 0;
		for (int i = 0; i < T.length; i++)
			sum += T[i];
		return sum;
	}

	public static double mean(double[] T) {
		return sum(T) / T.length;
	}

	/**
	 * Calculates the (population) variance of the values of a times series.
	 */
	public static double variance(double T[]) {
		double mean = mean(T);
		double squaredDeviations = 0;
		for (int i = 0; i < T.length; i++) {
			squaredDeviations += (T[i] - mean) * (T[i] - mean);
		}
		return squaredDeviations / T.length;
	}

	/**
	 * Calculates the (population) standard deviation of the values of a times
	 * series.
	 */
	public static double standardDeviation(double[] T) {
		return Math.sqrt(variance(T));
	}

	public static double[] zTransform(double[] T) {
		double mean = mean(T);
		double standardDeviation = standardDeviation(T);
		if (standardDeviation == 0) { // TODO: How to handle zero standard deviation properly.
			return new double[T.length]; // All zeros.
		}
		double[] zTransformedT = new double[T.length];
		for (int i = 0; i < T.length; i++) {
			zTransformedT[i] = (T[i] - mean) / standardDeviation;
		}
		return zTransformedT;
	}

	public static double[] normalizeByStandardDeviation(double[] T) {
		double standardDeviation = standardDeviation(T);
		if (standardDeviation == 0) {
			return new double[T.length];
		}
		double[] normalizedT = new double[T.length];
		for (int i = 0; i < T.length; i++) {
			normalizedT[i] = T[i] / standardDeviation;
		}
		return normalizedT;

	}

}