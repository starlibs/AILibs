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

}