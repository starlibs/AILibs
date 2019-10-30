package ai.libs.jaicore.ml.classification.singlelabel.timeseries.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.api4.java.ai.ml.core.exception.TrainingException;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * WekaUtil
 */
public class WekaTimeseriesUtil {

	private static final String I_NAME = "Instances";

	private WekaTimeseriesUtil() {
		/* no instantiation desired */
	}

	/**
	 * Maps an univariate simplified time series instance to a Weka instance.
	 *
	 * @param instance
	 *            The time series instance storing the time series data
	 * @return Returns the Weka instance containing the time series
	 */
	public static Instance simplifiedTSInstanceToWekaInstance(final double[] instance) {
		return new DenseInstance(1, instance);
	}

	/**
	 * Trains a given Weka <code>classifier</code> using the simplified time series
	 * data set <code>timeSeriesDataset</code>.
	 *
	 * @param classifier
	 *            The Weka {@link weka.Classifier} object
	 * @param timeSeriesDataset
	 *            The time series data set which is transformed to Weka instances
	 *            used for the training
	 * @throws TrainingException
	 *             Throws exception if the training could not be finished
	 *             successfully
	 */
	public static void buildWekaClassifierFromSimplifiedTS(final Classifier classifier, final ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2 timeSeriesDataset) throws TrainingException {

		final Instances trainingInstances = simplifiedTimeSeriesDatasetToWekaInstances(timeSeriesDataset);

		try {
			classifier.buildClassifier(trainingInstances);
		} catch (Exception e) {
			throw new TrainingException(String.format("Could not train classifier %s due to a Weka exception.", classifier.getClass().getName()), e);
		}
	}

	/**
	 * Converts a given simplified {@link ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2}
	 * object to a Weka Instances object.
	 *
	 * @param dataSet
	 *            Data set which is transformed
	 * @return Transformed Weka Instances object
	 */
	public static Instances simplifiedTimeSeriesDatasetToWekaInstances(final ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2 dataSet) {

		final int[] targets = dataSet.getTargets();
		List<Integer> targetList = Arrays.asList(ArrayUtils.toObject(targets));

		int min = Collections.min(targetList);
		int max = Collections.max(targetList);
		List<String> classValues = IntStream.rangeClosed(min, max).boxed().map(String::valueOf).collect(Collectors.toList());

		return simplifiedTimeSeriesDatasetToWekaInstances(dataSet, classValues);
	}

	/**
	 * Converts a given simplified {@link ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2}
	 * object to a Weka Instances object.
	 *
	 * @param dataSet
	 *            Data set which is transformed
	 * @return Transformed Weka Instances object
	 */
	public static Instances simplifiedTimeSeriesDatasetToWekaInstances(final ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2 dataSet, final List<String> classValues) {

		List<double[][]> matrices = new ArrayList<>();
		for (int i = 0; i < dataSet.getNumberOfVariables(); i++) {
			matrices.add(dataSet.getValues(i));
		}

		// Create attributes
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int m = 0; m < matrices.size(); m++) {
			double[][] matrix = matrices.get(m);
			if (matrix == null) {
				continue;
			}

			for (int i = 0; i < matrix[0].length; i++) {
				final Attribute newAtt = new Attribute(String.format("val_%d_%d", m, i));
				attributes.add(newAtt);
			}
		}

		// Add class attribute
		final int[] targets = dataSet.getTargets();
		attributes.add(new Attribute("class", classValues));
		final Instances result = new Instances(I_NAME, attributes, dataSet.getNumberOfInstances());
		result.setClassIndex(result.numAttributes() - 1);

		// Create instances
		for (int i = 0; i < dataSet.getNumberOfInstances(); i++) {

			double[] concatenatedRow = matrices.get(0)[i];
			for (int j = 1; j < matrices.size(); j++) {
				concatenatedRow = ArrayUtils.addAll(concatenatedRow, matrices.get(j)[i]);
			}

			concatenatedRow = ArrayUtils.addAll(concatenatedRow, targets[i]);

			// Initialize instance
			final Instance inst = new DenseInstance(1, concatenatedRow);
			inst.setDataset(result);
			result.add(inst);
		}

		return result;
	}

	/**
	 * Converts a double[][] matrix (number of instances x number of attributes) to
	 * Weka instances without any class attribute.
	 *
	 * @param matrix
	 *            The double[][] matrix storing all the attribute values of the
	 *            instances
	 * @return Returns the Weka Instances object consisting of all instances and the
	 *         attribute values
	 */
	public static Instances matrixToWekaInstances(final double[][] matrix) {
		final ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < matrix[0].length; i++) {
			final Attribute newAtt = new Attribute("val" + i);
			attributes.add(newAtt);
		}
		Instances wekaInstances = new Instances(I_NAME, attributes, matrix.length);
		for (int i = 0; i < matrix[0].length; i++) {
			final Instance inst = new DenseInstance(1, matrix[i]);
			inst.setDataset(wekaInstances);
			wekaInstances.add(inst);
		}

		return wekaInstances;
	}

}