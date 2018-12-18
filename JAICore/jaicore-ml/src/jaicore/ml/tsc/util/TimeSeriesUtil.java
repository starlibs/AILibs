package jaicore.ml.tsc.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;
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
		if (array.shape().length > 1)
			throw new IllegalArgumentException(
					String.format("Input INDArray object must be a vector with shape size 1. Actual shape: (%s)",
							Arrays.toString(array.shape())));

		final double mean = array.mean(0).getDouble(0);
		final double std = array.std(0).getDouble(0);

		INDArray result;
		if (inplace) {
			result = array.subi(mean);
		} else {
			result = array.sub(mean);
		}
		return result.addi(Nd4j.EPS_THRESHOLD).divi(std);
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

		int numInstances = (int) matrix.shape()[0];
		int numAttributes = (int) matrix.shape()[1];

		// Create attributes
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			final Attribute newAtt = new Attribute("val" + i);
			attributes.add(newAtt);
		}

		final Instances result = new Instances("Instances", attributes, numInstances);
		result.setClassIndex(result.numAttributes() - 1);

		for (int i = 0; i < numInstances; i++) {

			// Initialize instance
			final Instance inst = new DenseInstance(1, Nd4j.toFlattened(matrix.getRow(i)).toDoubleVector());
			inst.setDataset(result);

			result.add(inst);
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
}