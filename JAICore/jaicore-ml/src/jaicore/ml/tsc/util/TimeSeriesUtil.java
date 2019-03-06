package jaicore.ml.tsc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
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
	 * @param timeSeries
	 *            Time series to generate timestamps for. Let n be its length.
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
	 * @param timeSeries
	 *            Time series to generate timestamps for. Let n be its length.
	 * @return Equidistant timestamp, i.e. {0, 1, .., n-1}.
	 */
	public static double[] createEquidistantTimestamps(double[] timeSeries) {
		int n = timeSeries.length;
		double[] timestamps = IntStream.range(0, n).mapToDouble(t -> (double) t).toArray();
		return timestamps;
	}

	/**
	 * Function extracting the interval [start, end (exclusive)] out of the given
	 * <code>timeSeries</code> vector.
	 * 
	 * @param timeSeries
	 *            Time series vector source
	 * @param start
	 *            Start of the interval
	 * @param end
	 *            End index of the interval (exclusive)
	 * @return Returns the specified interval as a double array
	 */
	public static double[] getInterval(double[] timeSeries, int start, int end) {
		if (end <= start)
			throw new IllegalArgumentException("The end index must be greater than the start index.");

		final double[] result = new double[end - start];
		for (int j = 0; j < end - start; j++) {
			result[j] = timeSeries[j + start];
		}
		return result;
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
	 * Returns the mode of the given <code>array</code>. If there are multiple
	 * values with the same frequency, the lower value will be taken.
	 * 
	 * @param array
	 *            The array which mode should be returned
	 * @return Returns the mode, i. e. the most frequently occurring int value
	 */
	public static int getMode(final int[] array) {
		HashMap<Integer, Integer> statistics = new HashMap<>();
		for (int i = 0; i < array.length; i++) {
			if (!statistics.containsKey(array[i]))
				statistics.put(array[i], 1);
			else
				statistics.replace(array[i], statistics.get(array[i]) + 1);
		}

		int maxKey = getMaximumKeyByValue(statistics) != null ? getMaximumKeyByValue(statistics) : -1;
		return maxKey;
	}

	/**
	 * Returns the key with the maximum integer value. If there are multiple values
	 * with the same value, the lower key with regard to its type will be taken.
	 * 
	 * @param map
	 *            The map storing the keys with its corresponding integer values
	 * @return Returns the key of type <T> storing the maximum integer value
	 */
	public static <T> T getMaximumKeyByValue(final Map<T, Integer> map) {
		T maxKey = null;
		int maxCount = 0;
		for (T key : map.keySet()) {
			if (map.get(key) > maxCount) {
				maxCount = map.get(key);
				maxKey = key;
			}
		}

		return maxKey;
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
	// TODO: Unify with Helen's calculation
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

	/**
	 * Sorts the indices of the given <code>vector</code> based on the the vector's
	 * values (argsort).
	 * 
	 * @param vector
	 *            Vector where the values are extracted from
	 * @param ascending
	 *            Indicator whether the indices should be sorted ascending
	 * @return Returns the list of indices which are sorting based on the vector's
	 *         values
	 */
	public static List<Integer> sortIndexes(final double[] vector, final boolean ascending) {
		List<Integer> result = new ArrayList<>();

		Integer[] indexes = new Integer[(int) vector.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}

		Arrays.sort(indexes, new Comparator<Integer>() {
			@Override
			public int compare(final Integer i1, final Integer i2) {
				return (ascending ? 1 : -1) * Double.compare(Math.abs(vector[i1]), Math.abs(vector[i2]));
			}
		});

		for (int i = 0; i < indexes.length; i++) {
			result.add(indexes[i]);
		}

		return result;
	}

	/**
	 * Counts the number of unique classes occurring in the given
	 * <code>dataset</code>.
	 * 
	 * @param dataset
	 *            Dataset to be evaluated
	 * @return Returns the number of unique classes occurring in target matrix of
	 *         the given <code>dataset</code>
	 */
	public static int getNumberOfClasses(final TimeSeriesDataset dataset) {
		if (dataset == null || dataset.getTargets() == null)
			throw new IllegalArgumentException(
					"Given parameter 'dataset' must not be null and must contain a target matrix!");
		
		return getClassesInDataset(dataset).size();
	}

	/**
	 * Returns a list storing the unique Integer class values in the given
	 * <code>dataset</code>.
	 * 
	 * @param dataset
	 *            Dataset to be evaluated
	 * @return Returns a {@link java.util.List} object storing the unique Integer
	 *         class values of the dataset
	 */
	public static List<Integer> getClassesInDataset(final TimeSeriesDataset dataset) {
		if (dataset == null || dataset.getTargets() == null)
			throw new IllegalArgumentException(
					"Given parameter 'dataset' must not be null and must contain a target matrix!");
		
		return IntStream.of(dataset.getTargets()).boxed().collect(Collectors.toSet()).stream()
				.collect(Collectors.toList());
	}

	/**
	 * Shuffles the given {@link TimeSeriesDataset} object using the given
	 * <code>seed</code>.
	 * 
	 * @param dataset
	 *            The dataset to be shuffled
	 * @param seed
	 *            The seed used within the randomized shuffling
	 */
	public static void shuffleTimeSeriesDataset(final TimeSeriesDataset dataset, final int seed) {

		List<Integer> indices = IntStream.range(0, (int) dataset.getNumberOfInstances()).boxed()
				.collect(Collectors.toList());
		Collections.shuffle(indices, new Random(seed));

		List<double[][]> valueMatrices = dataset.getValueMatrices();
		List<double[][]> timestampMatrices = dataset.getTimestampMatrices();
		int[] targets = dataset.getTargets();

		if (valueMatrices != null) {
			List<double[][]> targetValueMatrices = new ArrayList<>();
			for (int i = 0; i < valueMatrices.size(); i++) {
				targetValueMatrices.add(shuffleMatrix(valueMatrices.get(i), indices));
			}
			dataset.setValueMatrices(targetValueMatrices);
		}

		if (timestampMatrices != null) {
			List<double[][]> targetTimestampMatrices = new ArrayList<>();
			for (int i = 0; i < timestampMatrices.size(); i++) {
				targetTimestampMatrices.add(shuffleMatrix(timestampMatrices.get(i), indices));
			}
			dataset.setTimestampMatrices(targetTimestampMatrices);
		}

		if (targets != null) {
			dataset.setTargets(shuffleMatrix(targets, indices));
		}
	}

	/**
	 * Shuffles the given <code>srcMatrix</code> using a list of Integer
	 * <code>indices</code>. It copies the values into a new result array sharing
	 * the dimensionality of <code>srcMatrix</code>.
	 * 
	 * @param srcMatrix
	 *            The source matrix to be shuffled
	 * @param indices
	 *            The Integer indices representing the new shuffled order
	 * @return Returns the matrix copied from the given source matrix and the
	 *         indices
	 */
	private static double[][] shuffleMatrix(final double[][] srcMatrix, final List<Integer> indices) {
		if (srcMatrix == null || srcMatrix.length < 1)
			throw new IllegalArgumentException("Parameter 'srcMatrix' must not be null or empty!");

		if (indices == null || indices.size() != srcMatrix.length)
			throw new IllegalArgumentException(
					"Parameter 'indices' must not be null and must have the same length as the number of instances in the source matrix!");

		final double[][] result = new double[srcMatrix.length][srcMatrix[0].length];
		for (int i = 0; i < indices.size(); i++) {
			result[i] = srcMatrix[indices.get(i)];
		}
		return result;
	}

	/**
	 * Shuffles the given <code>srcMatrix</code> using a list of Integer
	 * <code>indices</code>. It copies the values into a new result array sharing
	 * the dimensionality of <code>srcMatrix</code>.
	 * 
	 * @param srcMatrix
	 *            The source matrix to be shuffled
	 * @param indices
	 *            The Integer indices representing the new shuffled order
	 * @return Returns the matrix copied from the given source matrix and the
	 *         indices
	 */
	private static int[] shuffleMatrix(final int[] srcMatrix, final List<Integer> indices) {
		if (srcMatrix == null || srcMatrix.length < 1)
			throw new IllegalArgumentException("Parameter 'srcMatrix' must not be null or empty!");

		if (indices == null || indices.size() != srcMatrix.length)
			throw new IllegalArgumentException(
					"Parameter 'indices' must not be null and must have the same length as the number of instances in the source matrix!");

		final int[] result = new int[srcMatrix.length];
		for (int i = 0; i < indices.size(); i++) {
			result[i] = srcMatrix[indices.get(i)];
		}
		return result;
	}
}