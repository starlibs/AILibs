package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;

/**
 * Base class for simplified time series classifier comparisons between own
 * classes and reference implementations.
 * 
 * @author Julian Lienen
 *
 */
public class SimplifiedTSClassifierTest extends TSClassifierTest {
	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedTSClassifierTest.class);

	/**
	 * Function comparing a time series classifier deriving from Weka's
	 * <code>Classifier</code> interface (used in Bagnall's reference
	 * implementations) or from <code>sfa.classification.Classifier</code>.
	 * 
	 * @param tsRefClassifier
	 *            Time series classifier reference to be compared to own
	 *            implementation
	 * @param tsClassifier
	 *            Own implementation of the time series classifier
	 * @param seed
	 *            Seed used in the experiments
	 * @param tsRefClassifierParams
	 *            Textual description of the used parameters of the reference
	 *            classifier. Will be stored in the database entry
	 * @param tsClassifierParams
	 *            Textual description of the used parameters of the own classifier.
	 *            Will be stored in the database entry
	 * @param trainingArffFile
	 *            Arff file storing the training dataset
	 * @param testArffFile
	 *            Arff file storing the test dataset
	 * @return Returns a map consisting of a fields and values to be stored in the
	 *         database
	 * @throws IOException
	 *             Will be thrown if the data could not be read
	 * @throws FileNotFoundException
	 *             Will be thrown if the given data set file could not be found
	 * @throws EvaluationException
	 *             Will be thrown if the given classifier could not be evaluated
	 * @throws TrainingException
	 *             Will be thrown if the given classifier could not be trained
	 * @throws PredictionException
	 *             Will be thrown if a prediction error occurred
	 * @throws TimeSeriesLoadingException
	 *             Will be thrown if the time series dataset could not be loaded
	 */
	public static Map<String, Object> compareClassifiers(final Object tsRefClassifier,
			final ASimplifiedTSClassifier<?> tsClassifier, final int seed, final String tsRefClassifierParams,
			final String tsClassifierParams, final File trainingArffFile, final File testArffFile)
			throws FileNotFoundException, EvaluationException, TrainingException, IOException, PredictionException,
			TimeSeriesLoadingException {

		if (trainingArffFile == null || testArffFile == null)
			throw new IllegalArgumentException("Training and test file must not be null!");

		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", reduceFileNames(trainingArffFile, testArffFile));

		// Load dataset
		// TODO: Deal with strings?
		Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader.loadArff(trainingArffFile);
		TimeSeriesDataset train = trainPair.getX();
		tsClassifier.setClassMapper(trainPair.getY());
		Pair<TimeSeriesDataset, ClassMapper> testPair = SimplifiedTimeSeriesLoader.loadArff(testArffFile);
		TimeSeriesDataset test = testPair.getX();

		// TODO: Deal with it and move it to the right place
		if (trainPair.getY() != null && testPair.getY() != null
				&& !testPair.getY().getClassValues().equals(trainPair.getY().getClassValues())) {
			LOGGER.warn("The class mapper of the training data differs from the test class mapper.");
		}

		trainAndEvaluateClassifier(tsClassifier, seed, tsClassifierParams, result, train, test);

		// Test reference classifier
		compareRefClassifiers(tsRefClassifier, seed, tsRefClassifierParams, result, trainingArffFile, testArffFile);

		return result;
	}

	/**
	 * Function comparing a time series classifier deriving from Weka's
	 * <code>Classifier</code> interface (used in Bagnall's reference
	 * implementations) or from <code>sfa.classification.Classifier</code>.
	 * 
	 * @param tsRefClassifier
	 *            Time series classifier reference to be compared to own
	 *            implementation
	 * @param tsClassifier
	 *            Own implementation of the time series classifier
	 * @param seed
	 *            Seed used for the stratified split of the given data in
	 *            <code>arffFile</code>
	 * @param trainingPortion
	 *            Training portion to be used for training of both classifiers
	 * @param tsRefClassifierParams
	 *            Textual description of the used parameters of the reference
	 *            classifier. Will be stored in the database entry
	 * @param tsClassifierParams
	 *            Textual description of the used parameters of the own classifier.
	 *            Will be stored in the database entry
	 * @param arffFiles
	 *            Arff files containing the data used for training and evaluation
	 *            (assumes univariate dataset, if only one file is given)
	 * @return Returns a map consisting of a fields and values to be stored in the
	 *         database
	 * @throws IOException
	 *             Will be thrown if the data could not be read
	 * @throws FileNotFoundException
	 *             Will be thrown if the given data set file could not be found
	 * @throws EvaluationException
	 *             Will be thrown if the given classifier could not be evaluated
	 * @throws TrainingException
	 *             Will be thrown if the given classifier could not be trained
	 * @throws PredictionException
	 *             Will be thrown if a prediction error occurred
	 * @throws TimeSeriesLoadingException
	 *             Will be thrown if the time series dataset could not be loaded
	 */
	public static Map<String, Object> compareClassifiers(final Object tsRefClassifier,
			final ASimplifiedTSClassifier<?> tsClassifier, final int seed, final double trainingPortion,
			final String tsRefClassifierParams, final String tsClassifierParams, final File... arffFiles)
			throws FileNotFoundException, EvaluationException, TrainingException, IOException, PredictionException,
			TimeSeriesLoadingException {

		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", reduceFileNames(arffFiles));

		// Test reference classifier
		compareRefClassifiers(tsRefClassifier, seed, trainingPortion, tsRefClassifierParams, result, arffFiles);

		// Load dataset
		Pair<TimeSeriesDataset, ClassMapper> dataset = loadDatasetFromArffFiles(arffFiles);

		// TODO
		Pair<TimeSeriesDataset, TimeSeriesDataset> trainTest = null; // jaicore.ml.tsc.util.WekaUtil.getStratifiedSplit(dataset,
		// trainingPortion);
		TimeSeriesDataset train = trainTest.getX();
		TimeSeriesDataset test = trainTest.getY();

		trainAndEvaluateClassifier(tsClassifier, seed, tsClassifierParams, result, train, test);

		return result;
	}

	/**
	 * Trains and evaluates a given <code>TSClassifier</code> object with regard to
	 * the accuracy score using the given data from file <code>arffFile</code>
	 * partitioned into a fraction of <code>trainingPortion</code> as training data.
	 * 
	 * @param tsClassifier
	 *            Time series classifier to be trained and evaluated
	 * @param seed
	 *            Seed used for randomized splitting the given data into train and
	 *            test set
	 * @param tsClassifierParams
	 *            Textual description of the time series classifier parameters to be
	 *            stored in the database
	 * @param result
	 *            Map used to store the database entry's information
	 * @param train
	 *            Training instances
	 * @param test
	 *            Test instances
	 * @throws TrainingException
	 *             Will be thrown if the training of <code>tsClassifier</code> fails
	 * @throws PredictionException
	 *             Will be thrown if the prediction of <code>tsClassifier</code>
	 *             fails
	 */
	private static void trainAndEvaluateClassifier(final ASimplifiedTSClassifier<?> tsClassifier, final int seed,
			final String tsClassifierParams, final Map<String, Object> result, final TimeSeriesDataset train,
			final TimeSeriesDataset test) throws TrainingException, PredictionException {

		result.put("classifier", tsClassifier.getClass().getSimpleName());
		result.put("classifier_params", tsClassifierParams);

		// Training
		LOGGER.debug("Starting training of classifier...");
		long timeStart = System.currentTimeMillis();
		tsClassifier.train(train);
		final long trainingEnd = System.currentTimeMillis();
		LOGGER.debug("Finished training of classifier. Took {} ms.", (trainingEnd - timeStart));
		result.put("train_time", (trainingEnd - timeStart));

		// Evaluation
		LOGGER.debug("Starting evaluation of classifier...");
		timeStart = System.currentTimeMillis();

		List<?> predictions = tsClassifier.predict(test);
		int totalPreds = predictions.size();
		int correct = 0;

		if (totalPreds <= 0)
			throw new PredictionException("Nothing has been predicted.");

		if (!(predictions.get(0) instanceof Integer || predictions.get(0) instanceof String))
			throw new PredictionException("Can not evaluate classifier due to an unsupported target type.");

		if (predictions.get(0) instanceof Integer) {
			for (int i = 0; i < totalPreds; i++) {
				int prediction = (int) predictions.get(i);
				if (prediction == test.getTargets()[i])
					correct++;
			}
		} else if (predictions.get(0) instanceof String) {
			for (int i = 0; i < totalPreds; i++) {
				String prediction = (String) predictions.get(i);
				// TODO: Add mapper
				throw new UnsupportedOperationException("Not implemented yet.");
				// if (prediction.equals(test.getTargets()[i]))
				// correct++;
			}
		}

		double accuracy = (double) correct / totalPreds;

		final long evaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of classifier. Took {} ms. Accuracy: {}", (evaluationEnd - timeStart),
				accuracy);
		result.put("ref_eval_time", (evaluationEnd - timeStart));
		result.put("ref_accuracy", accuracy);
	}

	/**
	 * Loads a (simplified) time series dataset from an arbitrary number of arff
	 * files (multiple will be handled as multivariate dataset).
	 * 
	 * @param arffFiles
	 *            Arff files to be loaded
	 * @return Returns a TimeSeriesDataset object storing the arff data
	 * @throws TimeSeriesLoadingException
	 *             Thrown if the dataset could not be parsed
	 */
	private static Pair<TimeSeriesDataset, ClassMapper> loadDatasetFromArffFiles(final File... arffFiles)
			throws TimeSeriesLoadingException {
		Pair<TimeSeriesDataset, ClassMapper> result;

		try {
			if (arffFiles.length < 1)
				throw new IllegalArgumentException("At least one arff file must be given!");
			else if (arffFiles.length == 1) {
				result = SimplifiedTimeSeriesLoader.loadArff(arffFiles[0]);
			} else {
				result = SimplifiedTimeSeriesLoader.loadArffs(arffFiles);
			}
		} catch (TimeSeriesLoadingException e) {
			throw new TimeSeriesLoadingException("Could not load training dataset.", e);
		}
		return result;
	}
}
