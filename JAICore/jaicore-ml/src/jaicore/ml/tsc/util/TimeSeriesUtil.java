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
	 * Checks, whether a given INDArray is a valid time series.
	 * 
	 * @param array
	 * @return True, if the array is a valid time series.
	 */
	public static boolean isTimeSeries(INDArray array) {
		return array.rank() == 1;
	}

	/**
	 * Checks, whether a given INDArray is a valid time series with a given length.
	 * 
	 * @param array
	 * @param length
	 * @return True, if the array is a valid time series of the given length. False,
	 *         otherwise.
	 */
	public static boolean isTimeSeries(INDArray array, int length) {
		return array.rank() == 1 && array.length() == length;
	}

	/**
	 * Checks, whether a given INDArray is a valid time series. Throws an exception
	 * otherwise.
	 * 
	 * @param array
	 * @throws IllegalArgumentException
	 */
	public static void isTimeSeriesOrException(INDArray array) throws IllegalArgumentException {
		if (!isTimeSeries(array)) {
			String message = String.format(
					"The given INDArray is no time series. It should have rank 1, but has a rank of %d.", array.rank());
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Checks, wheter a given INDArray is a valid time series with a given length.
	 * Throws an exception otherwise.
	 * 
	 * @param array
	 * @param length
	 * @throws IllegalArgumentException
	 */
	public static void isTimeSeriesOrException(INDArray array, int length) throws IllegalArgumentException {
		if (!isTimeSeries(array)) {
			String message = String.format(
					"The given INDArray is no time series. It should have rank 1, but has a rank of %d.", array.rank());
			throw new IllegalArgumentException(message);
		}
		if (!isTimeSeries(array, length)) {
			String message = String.format("The given time series should length 7, but has a length of %d.",
					array.length());
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Checks whether arrays have the same length.
	 * 
	 * @param timeSeries1
	 * @param timeSeries2
	 * @return True if the arrays have the same length. False, otherwise.
	 */
	public static boolean isSameLength(INDArray timeSeries1, INDArray timeSeries2) {
		return timeSeries1.length() == timeSeries2.length();
	}

	/**
	 * Checks whether two arrays have the same length. Throws an exception
	 * otherwise.
	 * 
	 * @param timeSeries1
	 * @param timeSeries2
	 * @throws TimeSeriesLengthException
	 */
	public static void isSameLengthOrException(INDArray timeSeries1, INDArray timeSeries2)
			throws TimeSeriesLengthException {
		if (!isSameLength(timeSeries1, timeSeries2)) {
			String message = String.format(
					"Length of the given time series are not equal: Length first time series: (%d). Length of seconds time series: (%d)",
					timeSeries1.length(), timeSeries2.length());
			throw new TimeSeriesLengthException(message);
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

}