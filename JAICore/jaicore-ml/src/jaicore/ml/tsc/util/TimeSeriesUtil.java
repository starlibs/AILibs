package jaicore.ml.tsc.util;

import java.util.Arrays;
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
	 * Normalizes an INDArray vector object.
	 * 
	 * @param array
	 *            INDArray row vector with single shape dimension
	 * @param inplace
	 *            Indication whether the normalization should be performed in place
	 *            or on a new array copy
	 * @return Returns the view on the transformed INDArray (if inplace) or a
	 *         normalized copy of the input array (if not inplace)
	 */
	public static INDArray normalizeINDArray(final INDArray array, final boolean inplace) {
		if (array.shape().length > 2 && array.shape()[0] != 1)
			throw new IllegalArgumentException(
					String.format("Input INDArray object must be a vector with shape size 1. Actual shape: (%s)",
							Arrays.toString(array.shape())));

		final double mean = array.mean(1).getDouble(0);
		final double std = array.std(1).getDouble(0);

		INDArray result;
		if (inplace) {
			result = array.subi(mean);
		} else {
			result = array.sub(mean);
		}
		return result.addi(Nd4j.EPS_THRESHOLD).divi(std);
	}

	/**
	 * Z-normalizes a given <code>dataVector</code>. Uses Bessel's correction
	 * (1/(n-1) in the calculation of the standard deviation) if set.
	 * 
	 * @param dataVector
	 *            Vector to be z-normalized
	 * @param besselsCorrection
	 *            Indicator whether the std dev correction using n-1 instead of n
	 *            should be applied
	 * @return Z-normalized vector
	 */
	// TODO: Use Filter implementation
	public static double[] zNormalize(final double[] dataVector, final boolean besselsCorrection) {
		// TODO: Parameter checks...

		int n = dataVector.length - (besselsCorrection ? 1 : 0);

		double mean = 0; // dataVector.meanNumber().doubleValue();
		for (int i = 0; i < dataVector.length; i++) {
			mean += dataVector[i];
		}
		mean /= dataVector.length;

		// Use Bessel's correction to get the sample stddev
		double stddev = 0;
		for (int i = 0; i < dataVector.length; i++) {
			stddev += Math.pow(dataVector[i] - mean, 2);
		}
		stddev /= n;
		stddev = Math.sqrt(stddev);

		double[] result = new double[dataVector.length];
		if (stddev == 0.0)
			return result;

		for (int i = 0; i < result.length; i++) {
			result[i] = (dataVector[i] - mean) / stddev;
		}

		return result;
	}
}