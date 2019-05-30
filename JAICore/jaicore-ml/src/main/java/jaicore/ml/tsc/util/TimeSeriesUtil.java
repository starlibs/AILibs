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

import jaicore.basic.sets.SetUtil.Pair;
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
	public static boolean isTimeSeries(final INDArray... array) {
		for (INDArray a : array) {
			if (a.rank() != 1) {
				return false;
			}
		}
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
	public static boolean isTimeSeries(final int length, final INDArray... array) {
		for (INDArray a : array) {
			if (a.rank() != 1 && a.length() == length) {
				return false;
			}
		}
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
	public static boolean isTimeSeries(final int length, final double[]... array) {
		for (double[] a : array) {
			if (a.length != length) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks, whether given INDArrays are valid time series. Throws an exception
	 * otherwise.
	 *
	 * @param array
	 * @throws IllegalArgumentException
	 */
	public static void isTimeSeriesOrException(final INDArray... array) {
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
	public static void isTimeSeriesOrException(final int length, final INDArray... array) {
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
	public static void isTimeSeriesOrException(final int length, final double[]... array) {
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
	public static boolean isSameLength(final INDArray timeSeries1, final INDArray... timeSeries) {
		for (INDArray t : timeSeries) {
			if (timeSeries1.length() != t.length()) {
				return false;
			}
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
	public static boolean isSameLength(final double[] timeSeries1, final double[]... timeSeries) {
		for (double[] t : timeSeries) {
			if (timeSeries1.length != t.length) {
				return false;
			}
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
	public static void isSameLengthOrException(final INDArray timeSeries1, final INDArray... timeSeries) {
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
	public static void isSameLengthOrException(final double[] timeSeries1, final double[]... timeSeries) {
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
	public static INDArray createEquidistantTimestamps(final INDArray timeSeries) {
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
	public static double[] createEquidistantTimestamps(final double[] timeSeries) {
		int n = timeSeries.length;
		return IntStream.range(0, n).mapToDouble(t -> (double) t).toArray();
	}

	/**
	 * Function extracting the interval [start, end (exclusive)] out of the given
	 * <code>timeSeries</code> vector.
	 *
	 * @param timeSeries Time series vector source
	 * @param start      Start of the interval
	 * @param end        End index of the interval (exclusive)
	 * @return Returns the specified interval as a double array
	 */
	public static double[] getInterval(final double[] timeSeries, final int start, final int end) {
		if (end <= start) {
			throw new IllegalArgumentException("The end index must be greater than the start index.");
		}

		final double[] result = new double[end - start];
		for (int j = 0; j < end - start; j++) {
			result[j] = timeSeries[j + start];
		}
		return result;
	}

	/**
	 * Normalizes an INDArray vector object.
	 *
	 * @param array   INDArray row vector with single shape dimension
	 * @param inplace Indication whether the normalization should be performed in
	 *                place or on a new array copy
	 * @return Returns the view on the transformed INDArray (if inplace) or a
	 *         normalized copy of the input array (if not inplace)
	 */
	public static INDArray normalizeINDArray(final INDArray array, final boolean inplace) {
		if (array.shape().length > 2 && array.shape()[0] != 1) {
			throw new IllegalArgumentException(
					String.format("Input INDArray object must be a vector with shape size 1. Actual shape: (%s)",
							Arrays.toString(array.shape())));
		}

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
	 * @param array The array which mode should be returned
	 * @return Returns the mode, i. e. the most frequently occurring int value
	 */
	public static int getMode(final int[] array) {
		HashMap<Integer, Integer> statistics = new HashMap<>();
		for (int i = 0; i < array.length; i++) {
			if (!statistics.containsKey(array[i])) {
				statistics.put(array[i], 1);
			} else {
				statistics.replace(array[i], statistics.get(array[i]) + 1);
			}
		}

		return getMaximumKeyByValue(statistics) != null ? getMaximumKeyByValue(statistics) : -1;
	}

	/**
	 * Returns the key with the maximum integer value. If there are multiple values
	 * with the same value, the lower key with regard to its type will be taken.
	 *
	 * @param map The map storing the keys with its corresponding integer values
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
	 * @param dataVector        Vector to be z-normalized
	 * @param besselsCorrection Indicator whether the std dev correction using n-1
	 *                          instead of n should be applied
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
		if (stddev == 0.0) {
			return result;
		}

		for (int i = 0; i < result.length; i++) {
			result[i] = (dataVector[i] - mean) / stddev;
		}
		return result;
	}

	/**
	 * Sorts the indices of the given <code>vector</code> based on the the vector's
	 * values (argsort).
	 *
	 * @param vector    Vector where the values are extracted from
	 * @param ascending Indicator whether the indices should be sorted ascending
	 * @return Returns the list of indices which are sorting based on the vector's
	 *         values
	 */
	public static List<Integer> sortIndexes(final double[] vector, final boolean ascending) {
		List<Integer> result = new ArrayList<>();

		Integer[] indexes = new Integer[vector.length];
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
	 * @param dataset Dataset to be evaluated
	 * @return Returns the number of unique classes occurring in target matrix of
	 *         the given <code>dataset</code>
	 */
	public static int getNumberOfClasses(final TimeSeriesDataset dataset) {
		if (dataset == null || dataset.getTargets() == null) {
			throw new IllegalArgumentException(
					"Given parameter 'dataset' must not be null and must contain a target matrix!");
		}

		return getClassesInDataset(dataset).size();
	}

	/**
	 * Returns a list storing the unique Integer class values in the given
	 * <code>dataset</code>.
	 *
	 * @param dataset Dataset to be evaluated
	 * @return Returns a {@link java.util.List} object storing the unique Integer
	 *         class values of the dataset
	 */
	public static List<Integer> getClassesInDataset(final TimeSeriesDataset dataset) {
		if (dataset == null || dataset.getTargets() == null) {
			throw new IllegalArgumentException(
					"Given parameter 'dataset' must not be null and must contain a target matrix!");
		}

		return IntStream.of(dataset.getTargets()).boxed().collect(Collectors.toSet()).stream()
				.collect(Collectors.toList());
	}

	/**
	 * Shuffles the given {@link TimeSeriesDataset} object using the given
	 * <code>seed</code>.
	 *
	 * @param dataset The dataset to be shuffled
	 * @param seed    The seed used within the randomized shuffling
	 */
	public static void shuffleTimeSeriesDataset(final TimeSeriesDataset dataset, final int seed) {

		List<Integer> indices = IntStream.range(0, dataset.getNumberOfInstances()).boxed()
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
	 * @param srcMatrix The source matrix to be shuffled
	 * @param indices   The Integer indices representing the new shuffled order
	 * @return Returns the matrix copied from the given source matrix and the
	 *         indices
	 */
	private static double[][] shuffleMatrix(final double[][] srcMatrix, final List<Integer> indices) {
		if (srcMatrix == null || srcMatrix.length < 1) {
			throw new IllegalArgumentException("Parameter 'srcMatrix' must not be null or empty!");
		}

		if (indices == null || indices.size() != srcMatrix.length) {
			throw new IllegalArgumentException(
					"Parameter 'indices' must not be null and must have the same length as the number of instances in the source matrix!");
		}

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
	 * @param srcMatrix The source matrix to be shuffled
	 * @param indices   The Integer indices representing the new shuffled order
	 * @return Returns the matrix copied from the given source matrix and the
	 *         indices
	 */
	private static int[] shuffleMatrix(final int[] srcMatrix, final List<Integer> indices) {
		if (srcMatrix == null || srcMatrix.length < 1) {
			throw new IllegalArgumentException("Parameter 'srcMatrix' must not be null or empty!");
		}

		if (indices == null || indices.size() != srcMatrix.length) {
			throw new IllegalArgumentException(
					"Parameter 'indices' must not be null and must have the same length as the number of instances in the source matrix!");
		}

		final int[] result = new int[srcMatrix.length];
		for (int i = 0; i < indices.size(); i++) {
			result[i] = srcMatrix[indices.get(i)];
		}
		return result;
	}

	/**
	 * Functions creating two {@link TimeSeriesDataset} objects representing the
	 * training and test split for the given <code>fold</code> of a cross validation
	 * with <code>numFolds</code> many folds. Data is extracted (and copied) from
	 * the given <code>srcValueMatrix</code> and <code>srcTargetMatrix</code>. The
	 * function uses the two functions
	 * {@link TimeSeriesUtil#selectTrainingDataForFold(int, int, int, int, double[][], int[])}
	 * and
	 * {@link TimeSeriesUtil#selectTestDataForFold(int, int, int, int, double[][], int[])}.
	 *
	 * @param fold            The current fold for which the datasets should be
	 *                        prepared
	 * @param numFolds        Number of total folds using within the performed cross
	 *                        validation
	 * @param srcValueMatrix  Source dataset from which the instances are copied
	 * @param srcTargetMatrix Source targets from which the targets are copied
	 * @return Returns a pair consisting of the training and test dataset
	 */
	public static Pair<TimeSeriesDataset, TimeSeriesDataset> getTrainingAndTestDataForFold(final int fold,
			final int numFolds, final double[][] srcValueMatrix, final int[] srcTargetMatrix) {
		return new Pair<TimeSeriesDataset, TimeSeriesDataset>(
				selectTrainingDataForFold(fold, numFolds, srcValueMatrix, srcTargetMatrix),
				selectTestDataForFold(fold, numFolds, srcValueMatrix, srcTargetMatrix));
	}

	/**
	 * Generates the training dataset for a fold. See
	 * {@link TimeSeriesUtil#getTrainingAndTestDataForFold(int, int, int, int, double[][], int[])
	 * for further details.
	 *
	 * @param fold            The current fold for which the datasets should be
	 *                        prepared
	 * @param numFolds        Number of total folds using within the performed cross
	 *                        validation
	 * @param srcValueMatrix  Source dataset from which the instances are copied
	 * @param srcTargetMatrix Source targets from which the targets are copied
	 * @return Returns a pair consisting of the training and test dataset
	 */
	private static TimeSeriesDataset selectTrainingDataForFold(final int fold, final int numFolds,
			final double[][] srcValueMatrix, final int[] srcTargetMatrix) {

		int numTestInstsPerFold = (int) ((double) srcValueMatrix.length / (double) numFolds);

		double[][] destValueMatrix = new double[(numFolds - 1) * numTestInstsPerFold][srcValueMatrix[0].length];
		int[] destTargetMatrix = new int[(numFolds - 1) * numTestInstsPerFold];

		if (fold == 0) { // First fold
			System.arraycopy(srcValueMatrix, numTestInstsPerFold, destValueMatrix, 0,
					(numFolds - 1) * numTestInstsPerFold);

			System.arraycopy(srcTargetMatrix, numTestInstsPerFold, destTargetMatrix, 0,
					(numFolds - 1) * numTestInstsPerFold);

		} else if (fold == (numFolds - 1)) { // Last fold
			System.arraycopy(srcValueMatrix, 0, destValueMatrix, 0, (numFolds - 1) * numTestInstsPerFold);

			System.arraycopy(srcTargetMatrix, 0, destTargetMatrix, 0, (numFolds - 1) * numTestInstsPerFold);

		} else { // Inner folds
			System.arraycopy(srcValueMatrix, 0, destValueMatrix, 0, fold * numTestInstsPerFold);
			System.arraycopy(srcValueMatrix, (fold + 1) * numTestInstsPerFold, destValueMatrix,
					fold * numTestInstsPerFold, (numFolds - fold - 1) * numTestInstsPerFold);

			System.arraycopy(srcTargetMatrix, 0, destTargetMatrix, 0, fold * numTestInstsPerFold);
			System.arraycopy(srcTargetMatrix, (fold + 1) * numTestInstsPerFold, destTargetMatrix,
					fold * numTestInstsPerFold, (numFolds - fold - 1) * numTestInstsPerFold);
		}

		ArrayList<double[][]> valueMatrices = new ArrayList<>();
		valueMatrices.add(destValueMatrix);
		return new TimeSeriesDataset(valueMatrices, destTargetMatrix);
	}

	/**
	 * Generates the test dataset for a fold. See
	 * {@link TimeSeriesUtil#getTrainingAndTestDataForFold(int, int, int, int, double[][], int[])
	 * for further details.
	 *
	 * @param fold            The current fold for which the datasets should be
	 *                        prepared
	 * @param numFolds        Number of total folds using within the performed cross
	 *                        validation
	 * @param numClasses      Number of classes in the targets
	 * @param srcValueMatrix  Source dataset from which the instances are copied
	 * @param srcTargetMatrix Source targets from which the targets are copied
	 * @return Returns a pair consisting of the training and test dataset
	 */
	private static TimeSeriesDataset selectTestDataForFold(final int fold, final int numFolds,
			final double[][] srcValueMatrix, final int[] srcTargetMatrix) {

		int numTestInstsPerFold = (int) ((double) srcValueMatrix.length / (double) numFolds);

		double[][] currTestMatrix;
		int[] currTestTargetMatrix;
		if (fold == (numFolds - 1)) {
			int remainingLength = srcValueMatrix.length - (numFolds - 1) * numTestInstsPerFold;
			currTestMatrix = new double[remainingLength][srcValueMatrix[0].length];
			currTestTargetMatrix = new int[remainingLength];
		} else {
			currTestMatrix = new double[numTestInstsPerFold][srcValueMatrix[0].length];
			currTestTargetMatrix = new int[numTestInstsPerFold];
		}

		System.arraycopy(srcValueMatrix, fold * numTestInstsPerFold, currTestMatrix, 0, currTestMatrix.length);
		System.arraycopy(srcTargetMatrix, fold * numTestInstsPerFold, currTestTargetMatrix, 0,
				currTestTargetMatrix.length);

		ArrayList<double[][]> testValueMatrices = new ArrayList<>();
		testValueMatrices.add(currTestMatrix);
		return new TimeSeriesDataset(testValueMatrices, currTestTargetMatrix);
	}

	/**
	 * Function creating a {@link TimeSeriesDataset} object given the
	 * <code>targets</code> and one or multiple <code>valueMatrices</code>.
	 *
	 * @param targets       The target values of the instances
	 * @param valueMatrices One or more matrices storing the time series values
	 * @return Returns a {@link TimeSeriesDataset} object constructed out of the
	 *         given parameters
	 */
	public static TimeSeriesDataset createDatasetForMatrix(final int[] targets, final double[][]... valueMatrices) {
		if (valueMatrices.length == 0) {
			throw new IllegalArgumentException(
					"There must be at least one value matrix to generate a TimeSeriesDataset object!");
		}

		ArrayList<double[][]> values = new ArrayList<>();

		for (final double[][] matrix : valueMatrices) {
			values.add(matrix);
		}
		return targets == null ? new TimeSeriesDataset(values) : new TimeSeriesDataset(values, targets);
	}

	/**
	 * Function creating a {@link TimeSeriesDataset} object given one or multiple
	 * <code>valueMatrices</code>.
	 *
	 * @param valueMatrices One or more matrices storing the time series values
	 * @return Returns a {@link TimeSeriesDataset} object constructed out of the
	 *         given parameters
	 */
	public static TimeSeriesDataset createDatasetForMatrix(final double[][]... valueMatrices) {
		return createDatasetForMatrix(null, valueMatrices);
	}

	/**
	 * Enables printing of time series.
	 *
	 * @param timeSeries Time series to print.
	 * @return Readable string of the time series, i.e.
	 *         <code>"{1.0, 2.0, 3.0, 4.0}"</code>
	 */
	public static String toString(final double[] timeSeries) {
		if (timeSeries.length == 0) {
			return "{}";
		}

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
	 * @param t
	 * @return
	 */
	public static double[] keoghDerivate(final double[] t) {
		double[] derivate = new double[t.length - 2];

		for (int i = 1; i < t.length - 1; i++) {
			derivate[i - 1] = ((t[i] - t[i - 1]) + (t[i + 1] - t[i - 1]) / 2) / 2;
		}

		return derivate;
	}

	/**
	 * Calculates the derivateive of a timeseries as described first by Keogh and
	 * Pazzani (2001).
	 * <code>f'(n) = \frac{ f(n) - f(n-1) + /frac{f(i+1) - f(i-1)}{2} }{2}</code>
	 *
	 * @param t
	 * @return
	 */
	public static double[] keoghDerivateWithBoundaries(final double[] t) {
		double[] derivate = new double[t.length];

		for (int i = 1; i < t.length - 1; i++) {
			derivate[i] = ((t[i] - t[i - 1]) + (t[i + 1] - t[i - 1]) / 2) / 2;
		}

		derivate[0] = derivate[1];
		derivate[t.length - 1] = derivate[t.length - 2];

		return derivate;
	}

	/**
	 * Calclualtes f'(n) = f(n-1) - f(n)
	 *
	 * @param t Time series.
	 * @return
	 */
	public static double[] backwardDifferenceDerivate(final double[] t) {
		double[] derivate = new double[t.length - 1];

		for (int i = 1; i < t.length; i++) {
			derivate[i - 1] = t[i] - t[i - 1];
		}

		return derivate;
	}

	/**
	 * Calclualtes f'(n) = f(n-1) - f(n)
	 *
	 * @param t Time series.
	 * @return
	 */
	public static double[] backwardDifferenceDerivateWithBoundaries(final double[] t) {
		double[] derivate = new double[t.length];

		for (int i = 1; i < t.length; i++) {
			derivate[i] = t[i] - t[i - 1];
		}

		derivate[0] = derivate[1];
		return derivate;
	}

	/**
	 * f'(n) = f(n+1) - f(n)
	 *
	 * @param t
	 * @return
	 */
	public static double[] forwardDifferenceDerivate(final double[] t) {
		double[] derivate = new double[t.length - 1];

		for (int i = 0; i < t.length - 1; i++) {
			derivate[i] = t[i + 1] - t[i];
		}

		return derivate;
	}

	/**
	 * f'(n) = f(n+1) - f(n)
	 *
	 * @param t
	 * @return
	 */
	public static double[] forwardDifferenceDerivateWithBoundaries(final double[] t) {
		double[] derivate = new double[t.length];

		for (int i = 0; i < t.length - 1; i++) {
			derivate[i] = t[i + 1] - t[i];
		}

		derivate[t.length - 1] = derivate[t.length - 2];
		return derivate;
	}

	/**
	 * Calculates the derivative of a timeseries as described first by Gullo et. al
	 * (2009).
	 *
	 * @param t
	 * @return
	 */
	public static double[] gulloDerivate(final double[] t) {
		double[] derivate = new double[t.length - 1];

		for (int i = 1; i < t.length; i++) {
			derivate[i - 1] = t[i + 1] - t[i - 1] / 2;
		}

		return derivate;
	}

	/**
	 * f'(n) = \frac{f(i+1)-f(i-1)}{2}
	 *
	 * @param t
	 * @return
	 */
	public static double[] gulloDerivateWithBoundaries(final double[] t) {
		double[] derivate = new double[t.length];

		for (int i = 1; i < t.length; i++) {
			derivate[i] = t[i + 1] - t[i - 1] / 2;
		}

		derivate[0] = derivate[1];
		return derivate;
	}

	public static double sum(final double[] t) {
		double sum = 0;
		for (int i = 0; i < t.length; i++) {
			sum += t[i];
		}
		return sum;
	}

	public static double mean(final double[] t) {
		return sum(t) / t.length;
	}

	/**
	 * Calculates the (population) variance of the values of a times series.
	 */
	public static double variance(final double[] t) {
		double mean = mean(t);
		double squaredDeviations = 0;
		for (int i = 0; i < t.length; i++) {
			squaredDeviations += (t[i] - mean) * (t[i] - mean);
		}
		return squaredDeviations / t.length;
	}

	/**
	 * Calculates the (population) standard deviation of the values of a times
	 * series.
	 */
	public static double standardDeviation(final double[] t) {
		return Math.sqrt(variance(t));
	}

	public static double EPSILON = 0.0000001;

	public static double[] zTransform(final double[] t) {
		double mean = mean(t);
		double standardDeviation = standardDeviation(t);
		// TODO: How to handle zero standard deviation properly.
		if ((-EPSILON < standardDeviation) && (standardDeviation < EPSILON)) {
			return new double[t.length]; // All zeros.
		}
		double[] zTransformedT = new double[t.length];
		for (int i = 0; i < t.length; i++) {
			zTransformedT[i] = (t[i] - mean) / standardDeviation;
		}
		return zTransformedT;
	}

	public static double[] normalizeByStandardDeviation(final double[] t) {
		double standardDeviation = standardDeviation(t);
		if (standardDeviation == 0) {
			return new double[t.length];
		}
		double[] normalizedT = new double[t.length];
		for (int i = 0; i < t.length; i++) {
			normalizedT[i] = t[i] / standardDeviation;
		}
		return normalizedT;

	}
}