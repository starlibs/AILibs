package jaicore.ml.tsc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * TimeSeriesUtil
 */
public class TimeSeriesUtil {

	public static void sameLengthOrException(TimeSeriesAttributeValue timeSeries1, TimeSeriesAttributeValue timeSeries2)
			throws TimeSeriesLengthException {
		long length1 = timeSeries1.getValue().length();
		long length2 = timeSeries2.getValue().length();
		if (length1 != length2) {
			String message = String.format(
					"Length of the given time series are not equal: Length first time series: (%d). Length of seconds time series: (%d)",
					length1, length2);
			throw new TimeSeriesLengthException(message);
		}
	}

	/**
	 * Converts an INDArray matrix (number of instances x number of attributes) to
	 * Weka instances without any class attribute.
	 * 
	 * @param matrix
	 *            INDArray matrix storing all the attribute values of the instances
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
	 * @param dataSet
	 *            Data set which is transformed
	 * @return Transformed Weka Instances object
	 */
	// TODO: Include meta information
	public static Instances timeSeriesDatasetToWekaInstances(final TimeSeriesDataset dataSet) {
		if (!(dataSet.getTargetType(String.class) instanceof CategoricalAttributeType)) {
			throw new UnsupportedOperationException(
					"Time series to Weka instances transformation works only with categorical target attribute types.");
		}

		// Get all attributes
		List<IAttributeType<?>> attTypes = dataSet.getAttributeTypes();
		List<INDArray> matrices = new ArrayList<>();
		for (IAttributeType<?> attType : attTypes) {
			if (attType instanceof TimeSeriesAttributeType)
				matrices.add(dataSet.getMatrixForAttributeType((TimeSeriesAttributeType) attType));
		}

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
		attributes.add(new Attribute("class",
				new ArrayList<>(((CategoricalAttributeType) dataSet.getTargetType(String.class)).getDomain())));
		final Instances result = new Instances("Instances", attributes, dataSet.size());
		result.setClassIndex(result.numAttributes() - 1);

		// Concatenate multiple matrices if series is multivariate
		INDArray combinedMatrix;
		if (matrices.size() > 0) {
			combinedMatrix = matrices.get(0).dup();
			for (int i = 1; i < matrices.size(); i++) {
				combinedMatrix = Nd4j.hstack(combinedMatrix, matrices.get(i));
			}
		} else {
			combinedMatrix = Nd4j.create(0, 0);
		}

		// Create instances
		for (int i = 0; i < dataSet.size(); i++) {

			// Initialize instance
			final Instance inst = new DenseInstance(1, Nd4j.toFlattened(combinedMatrix.getRow(i)).toDoubleVector());

			inst.setDataset(result);
			inst.setClassValue(dataSet.get(i).getTargetValue(String.class).getValue());
		}

		return result;
	}

	/**
	 * Converts Weka instances to an INDArray matrix.
	 * 
	 * @param instances
	 *            Weka instances to be converted.
	 * @param keepClass
	 *            Determines whether the class attribute should be stored in the
	 *            result matrix
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
	 * @param classifier
	 *            The Weka {@link weka.Classifier} object
	 * @param matrix
	 *            The time series data set which is transformed to Weka instances
	 *            used for the training
	 * @throws TrainingException
	 *             Throws exception if the training could not be finished
	 *             successfully
	 */
	private static void buildWekaClassifierFromTS(final Classifier classifier,
			final TimeSeriesDataset timeSeriesDataset) throws TrainingException {

		final Instances trainingInstances = timeSeriesDatasetToWekaInstances(timeSeriesDataset);

		try {
			classifier.buildClassifier(trainingInstances);
		} catch (Exception e) {
			throw new TrainingException(String.format("Could not train classifier %d due to a Weka exception.",
					classifier.getClass().getName()), e);
		}
	}
}