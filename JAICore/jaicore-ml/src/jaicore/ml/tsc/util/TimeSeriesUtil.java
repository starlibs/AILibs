package jaicore.ml.tsc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Utility class for time series operations.
 */
public class TimeSeriesUtil {

	/**
	 * Checks, wheter given INDArray are valid time series.
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
	 * Checks, wheter given INDArrays are valid time series with a given length.
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
	 * Checks, wheter given INDArrays are valid time series. Throws an exception
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
	 * Checks, wheter given INDArrays are valid time series with a given length.
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
	 * Checks wheter multiple arrays have the same length.
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
	 * Checks wheter multiple arrays have the same length. Throws an exception
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

	public static Pair<TimeSeriesDataset, TimeSeriesDataset> getStratifiedSplit(final TimeSeriesDataset dataset,
			final double portion) {
		// TODO
		return new Pair<TimeSeriesDataset, TimeSeriesDataset>(null, null);
	}

	/**
	 * Converts an INDArray matrix (number of instances x number of attributes) to
	 * Weka instances without any class attribute.
	 * 
	 * @param matrix INDArray matrix storing all the attribute values of the
	 *               instances
	 * @return Returns the Weka Instances object consisting of all instances and the
	 *         attribute values
	 */
	public static Instances indArrayToWekaInstances(final INDArray matrix) {
		if (matrix == null || matrix.length() == 0) {
			throw new IllegalArgumentException("Matrix must not be null or empty!");
		}
		if (matrix.shape().length != 2)
			throw new IllegalArgumentException(String.format(
					"Parameter matrix must be a matrix with 2 axis (instances x attributes). Actual shape: (%s)",
					Arrays.toString(matrix.shape())));

		final int numInstances = (int) matrix.shape()[0];
		final int numAttributes = (int) matrix.shape()[1];

		// Create attributes
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			final Attribute newAtt = new Attribute("val" + i);
			attributes.add(newAtt);
		}

		final Instances result = new Instances("Instances", attributes, numInstances);

		for (int i = 0; i < numInstances; i++) {

			// Initialize instance
			final Instance inst = new DenseInstance(1, Nd4j.toFlattened(matrix.getRow(i)).toDoubleVector());
			inst.setDataset(result);

			result.add(inst);
		}

		return result;
	}

	/**
	 * Converts a given {@link TimeSeriesDataset} object to a Weka Instances object.
	 * Works with {@link CategoricalAttributeType} target values.
	 * 
	 * @param dataSet Data set which is transformed
	 * @return Transformed Weka Instances object
	 */
	// TODO: Include meta information
	public static Instances timeSeriesDatasetToWekaInstances(final TimeSeriesDataset dataSet) {

		// TODO: Integrate direct access in TimeSeriesDataset
		List<INDArray> matrices = new ArrayList<>();
		for (int i = 0; i < dataSet.getNumberOfVariables(); i++)
			matrices.add(dataSet.getValues(i));

		// Create attributes
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int m = 0; m < matrices.size(); m++) {
			INDArray matrix = matrices.get(m);
			for (int i = 0; i < matrix.shape()[1]; i++) {
				final Attribute newAtt = new Attribute(String.format("val_%d_%d", m, i));
				attributes.add(newAtt);
			}
		}

		// Add class attribute
		final INDArray targets = dataSet.getTargets();
		attributes.add(new Attribute("class",
				IntStream.rangeClosed((int) targets.minNumber().longValue(), (int) targets.maxNumber().longValue())
						.boxed().map(i -> String.valueOf(i)).collect(Collectors.toList())));
		final Instances result = new Instances("Instances", attributes, (int) dataSet.getNumberOfInstances());
		result.setClassIndex(result.numAttributes() - 1);

		// Concatenate multiple matrices if series is multivariate
		INDArray combinedMatrix = hstackINDArrays(matrices);

		// Create instances
		for (int i = 0; i < dataSet.getNumberOfInstances(); i++) {

			// Initialize instance
			final Instance inst = new DenseInstance(1, Nd4j.hstack(Nd4j.toFlattened(combinedMatrix.getRow(i)),
					Nd4j.create(new double[] { targets.getDouble(i) })).toDoubleVector());

			inst.setDataset(result);
			result.add(inst);
		}

		return result;
	}

	/**
	 * Converts Weka instances to an INDArray matrix.
	 * 
	 * @param instances Weka instances to be converted.
	 * @param keepClass Determines whether the class attribute should be stored in
	 *                  the result matrix
	 * @return Returns an INDArray consisting of all instances with the shape
	 *         (number instances x number attributes)
	 */
	public static INDArray wekaInstancesToINDArray(final Instances instances, final boolean keepClass) {
		if (instances == null || instances.size() == 0)
			throw new IllegalArgumentException("Instances must not be null or empty!");

		int classSub = keepClass ? 0 : (instances.classIndex() > -1 ? 1 : 0);
		int numAttributes = instances.numAttributes() - classSub;
		int numInstances = instances.numInstances();

		INDArray result = Nd4j.create(numInstances, numAttributes);

		for (int i = 0; i < numInstances; i++) {
			double[] instValues = instances.get(i).toDoubleArray();
			for (int j = 0; j < numAttributes; j++) {
				result.putScalar(new int[] { i, j }, instValues[j]);
			}
		}

		return result;
	}

	/**
	 * Trains a given Weka <code>classifier</code> using the time series data set
	 * <code>matrix</code>.
	 * 
	 * @param classifier The Weka {@link weka.Classifier} object
	 * @param matrix     The time series data set which is transformed to Weka
	 *                   instances used for the training
	 * @throws TrainingException Throws exception if the training could not be
	 *                           finished successfully
	 */
	public static void buildWekaClassifierFromTS(final Classifier classifier, final TimeSeriesDataset timeSeriesDataset)
			throws TrainingException {

		final Instances trainingInstances = timeSeriesDatasetToWekaInstances(timeSeriesDataset);

		try {
			classifier.buildClassifier(trainingInstances);
		} catch (Exception e) {
			throw new TrainingException(String.format("Could not train classifier %d due to a Weka exception.",
					classifier.getClass().getName()), e);
		}
	}

	/**
	 * Maps a time series instance to a Weka instance.
	 * 
	 * @param instance The time series instance storing the time series data and the
	 *                 target value
	 * @return Returns the Weka instance containing the time series data and the
	 *         class information.
	 */
	// TODO: Add meta attribute support
	public static Instance tsInstanceToWekaInstance(final TimeSeriesInstance instance) {
		List<IAttributeValue<?>> attValues = instance.getAttributeValues();
		List<INDArray> indArrays = new ArrayList<>();

		for (final IAttributeValue<?> attValue : attValues) {
			if (attValue instanceof TimeSeriesAttributeValue) {
				indArrays.add(((TimeSeriesAttributeValue) attValue).getValue());
			}
		}

		INDArray combinedMatrix = hstackINDArrays(indArrays);

		final Instance finalInstance = new DenseInstance(1, Nd4j.toFlattened(combinedMatrix).toDoubleVector());
		finalInstance.setClassValue(instance.getTargetValue(String.class).getValue());
		return finalInstance;
	}

	/**
	 * Stacks the given matrices horizontally.
	 * 
	 * @param matrices List of INDArray matrices to be stacked
	 * @return Returns one INDArray containing all <code>matrices</code>. New
	 *         dimensionality is (originalShape[0] x sum of originalShape[1]s)
	 */
	private static INDArray hstackINDArrays(List<INDArray> matrices) {
		// Check first shape dimension
		if (matrices.size() > 0) {
			long[] shape = matrices.get(0).shape();
			for (int i = 1; i < matrices.size(); i++) {
				if (matrices.get(i).shape()[0] != shape[0])
					throw new IllegalArgumentException("First dimensionality of the given matrices must be equal!");
			}
		}

		INDArray combinedMatrix;
		if (matrices.size() > 0) {
			combinedMatrix = matrices.get(0).dup();
			for (int i = 1; i < matrices.size(); i++) {
				combinedMatrix = Nd4j.hstack(combinedMatrix, matrices.get(i));
			}
		} else {
			// If an empty list was given, return an empty matrix
			combinedMatrix = Nd4j.create(0, 0);
		}
		return combinedMatrix;

	}
}