package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.TimeSeriesLoader;
import sfa.classification.Classifier.Predictions;
import sfa.timeseries.TimeSeries;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * Base class for time series classifier comparisons between own classes and
 * reference implementations.
 * 
 * @author Julian Lienen
 *
 */
public class TSClassifierTest {

	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TSClassifierTest.class);

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
			final TSClassifier<?, ?, TimeSeriesDataset> tsClassifier, final int seed,
			final String tsRefClassifierParams, final String tsClassifierParams, final File trainingArffFile,
			final File testArffFile) throws FileNotFoundException, EvaluationException, TrainingException, IOException,
			PredictionException, TimeSeriesLoadingException {

		if (trainingArffFile == null || testArffFile == null)
			throw new IllegalArgumentException("Training and test file must not be null!");

		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", reduceFileNames(trainingArffFile, testArffFile));

		// Load dataset
		TimeSeriesDataset train = TimeSeriesLoader.loadArff(trainingArffFile);
		TimeSeriesDataset test = TimeSeriesLoader.loadArff(testArffFile);

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
			final TSClassifier<?, ?, TimeSeriesDataset> tsClassifier, final int seed, final double trainingPortion,
			final String tsRefClassifierParams, final String tsClassifierParams, final File... arffFiles)
			throws FileNotFoundException, EvaluationException, TrainingException, IOException, PredictionException,
			TimeSeriesLoadingException {

		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", reduceFileNames(arffFiles));

		// Test reference classifier
		compareRefClassifiers(tsRefClassifier, seed, trainingPortion, tsRefClassifierParams, result, arffFiles);

		// Load dataset
		TimeSeriesDataset dataset = loadDatasetFromArffFiles(arffFiles);

		// TODO
		Pair<TimeSeriesDataset, TimeSeriesDataset> trainTest = null; // jaicore.ml.tsc.util.WekaUtil.getStratifiedSplit(dataset,
		// trainingPortion);
		TimeSeriesDataset train = trainTest.getX();
		TimeSeriesDataset test = trainTest.getY();

		trainAndEvaluateClassifier(tsClassifier, seed, tsClassifierParams, result, train, test);

		return result;
	}

	/**
	 * Function to add the training and test results to the given
	 * <code>result</code> map for the reference classifier.
	 * 
	 * @param tsRefClassifier
	 *            The time series reference classifier to be tested
	 * @param tsRefClassifierParams
	 *            The classifier parameters of the given reference classifier
	 *            (stored in the databse entry, the classifier is assumed to be
	 *            parameterized beforehand)
	 * @param result
	 *            The result map storing all experiment information
	 * @param trainingFile
	 *            The arff file storing the training data
	 * @param testFile
	 *            The arff file storing the test data
	 * @throws FileNotFoundException
	 *             Thrown if any of the dataset files could not be found
	 * @throws EvaluationException
	 *             Thrown if an error occurred during the evaluation
	 * @throws TrainingException
	 *             Thrown if the training could not be completed successfully
	 * @throws IOException
	 *             Thrown if an IO error occurred during the parsing of the arff
	 *             files
	 */
	public static void compareRefClassifiers(final Object tsRefClassifier, final int seed,
			final String tsRefClassifierParams, final Map<String, Object> result, final File trainingFile,
			final File testFile) throws FileNotFoundException, EvaluationException, TrainingException, IOException {
		if (tsRefClassifier instanceof sfa.classification.Classifier) {
			// SFA
			// TODO
			TimeSeries[] train = null;
			TimeSeries[] test = null;

			trainAndEvaluateSFARefClassifier((sfa.classification.Classifier) tsRefClassifier, tsRefClassifierParams,
					result, train, test);
		} else if (tsRefClassifier instanceof Classifier) {
			// Bagnall
			// Transform and split data to Weka instances
			ArffReader arffReader = new ArffReader(new FileReader(trainingFile));
			final Instances trainingInstances = arffReader.getData();
			trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);

			arffReader = new ArffReader(new FileReader(testFile));
			final Instances testInstances = arffReader.getData();
			testInstances.setClassIndex(testInstances.numAttributes() - 1);

			trainAndEvaluateBagnallRefClassifier((Classifier) tsRefClassifier, seed, tsRefClassifierParams, result,
					trainingInstances, testInstances);
		} else
			throw new IllegalArgumentException("Unknown reference classifier class.");
	}

	/**
	 * Function to add the training and test results to the given
	 * <code>result</code> map for the reference classifier.
	 * 
	 * @param tsRefClassifier
	 *            The time series reference classifier to be tested
	 * @param seed
	 *            The seed used for the data split
	 * @param trainingPortion
	 *            The training portion used for the data split
	 * @param tsRefClassifierParams
	 *            The classifier parameters of the given reference classifier
	 *            (stored in the databse entry, the classifier is assumed to be
	 *            parameterized beforehand)
	 * @param result
	 *            The result map storing all experiment information
	 * @param arffFiles
	 *            The arff files to be parsed
	 * @throws FileNotFoundException
	 *             Thrown if any of the dataset files could not be found
	 * @throws EvaluationException
	 *             Thrown if an error occurred during the evaluation
	 * @throws TrainingException
	 *             Thrown if the training could not be completed successfully
	 * @throws IOException
	 *             Thrown if an IO error occurred during the parsing of the arff
	 *             files
	 */
	public static void compareRefClassifiers(final Object tsRefClassifier, final int seed, final double trainingPortion,
			final String tsRefClassifierParams, final Map<String, Object> result, final File... arffFiles)
			throws FileNotFoundException, EvaluationException, TrainingException, IOException {
		if (tsRefClassifier instanceof sfa.classification.Classifier) {
			// SFA
			// TODO
			TimeSeries[] train = null;
			TimeSeries[] test = null;

			trainAndEvaluateSFARefClassifier((sfa.classification.Classifier) tsRefClassifier, tsRefClassifierParams,
					result, train, test);
		} else if (tsRefClassifier instanceof Classifier) {
			// Bagnall
			// Transform and split data to Weka instances
			if (arffFiles == null || arffFiles.length < 1)
				throw new IllegalArgumentException("At least one arff file must be given!");
			if (arffFiles.length > 1) {
				// TODO: Support multivariate evaluation
				throw new UnsupportedOperationException(
						"Multivariate ref ts classifier evaluation is not supported yet.");
			}
			ArffReader arffReader = new ArffReader(new FileReader(arffFiles[0]));
			final Instances wekaInstances = arffReader.getData();
			wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);
			List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, seed, trainingPortion);

			trainAndEvaluateBagnallRefClassifier((Classifier) tsRefClassifier, seed, tsRefClassifierParams, result,
					split.get(0), split.get(1));
		} else
			throw new IllegalArgumentException("Unknown reference classifier class.");
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
	private static void trainAndEvaluateClassifier(final TSClassifier<?, ?, TimeSeriesDataset> tsClassifier,
			final int seed, final String tsClassifierParams, final Map<String, Object> result,
			final TimeSeriesDataset train, final TimeSeriesDataset test) throws TrainingException, PredictionException {

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

		if (tsClassifier.getTargetType().getClass() != test.getTargetType().getClass())
			throw new PredictionException("Can not evaluate classifier due to wrong target type.");

		if (tsClassifier.getTargetType() instanceof CategoricalAttributeType) {
			CategoricalAttributeType targetType = (CategoricalAttributeType) tsClassifier.getTargetType();

			for (int i = 0; i < totalPreds; i++) {
				CategoricalAttributeValue prediction = (CategoricalAttributeValue) predictions.get(i);
				if (targetType.getDomain().indexOf(prediction.getValue()) == test.getTargets().getInt(i))
					correct++;
			}
		} else if (tsClassifier.getTargetType() instanceof NumericAttributeType) {
			for (int i = 0; i < totalPreds; i++) {
				NumericAttributeValue prediction = (NumericAttributeValue) predictions.get(i);
				if (prediction.getValue().intValue() == test.getTargets().getDouble(i))
					correct++;
			}
		}

		double accuracy = (double) correct / totalPreds;

		final long evaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of classifier. Took {} ms. Accuracy: {}", (evaluationEnd - timeStart),
				accuracy);
		result.put("eval_time", (evaluationEnd - timeStart));
		result.put("accuracy", accuracy);
	}

	/**
	 * Trains and evaluates a given <code>Classifier</code> object with regard to
	 * the accuracy score using the given data from file <code>arffFile</code>
	 * partitioned into a fraction of <code>trainingPortion</code> as training data.
	 * 
	 * @param tsRefClassifier
	 *            Time series classifier reference to be trained and evaluated
	 * @param seed
	 *            Seed used for randomized splitting the given data into train and
	 *            test set
	 * @param tsRefClassifierParams
	 *            Textual description of the time series classifier reference
	 *            parameters to be stored in the database
	 * @param result
	 *            Map used to store the database entry's information
	 * @param trainingInstances
	 *            Training instances
	 * @param testInstances
	 *            Test instances
	 * @throws FileNotFoundException
	 *             Will be thrown if the given data set file could not be found
	 * @throws IOException
	 *             Will be thrown if the data could not be read
	 * @throws EvaluationException
	 *             Will be thrown if the given classifier could not be evaluated
	 * @throws TrainingException
	 *             Will be thrown if the given classifier could not be trained
	 */
	protected static void trainAndEvaluateBagnallRefClassifier(final Classifier tsRefClassifier, final int seed,
			final String tsRefClassifierParams, final Map<String, Object> result, final Instances trainingInstances,
			final Instances testInstances)
			throws FileNotFoundException, IOException, EvaluationException, TrainingException {

		result.put("ref_classifier", tsRefClassifier.getClass().getSimpleName());
		result.put("ref_classifier_params", tsRefClassifierParams);

		// Training
		LOGGER.debug("Starting training of reference classifier...");
		long refTimeStart = System.currentTimeMillis();
		try {
			tsRefClassifier.buildClassifier(trainingInstances);
		} catch (Exception e) {
			LOGGER.debug("Could not train classifier {} due to {}.", tsRefClassifier.toString(), e.getMessage());
			throw new TrainingException("Could not train classifier " + tsRefClassifier.toString(), e);
		}
		final long refTrainingEnd = System.currentTimeMillis();
		LOGGER.debug("Finished training of reference classifier. Took {} ms.", (refTrainingEnd - refTimeStart));
		result.put("ref_train_time", (refTrainingEnd - refTimeStart));

		// Evaluation
		LOGGER.debug("Starting evaluation of reference classifier...");
		refTimeStart = System.currentTimeMillis();
		Evaluation refEvaluation;
		try {
			refEvaluation = new Evaluation(trainingInstances);
			refEvaluation.evaluateModel(tsRefClassifier, testInstances);
		} catch (Exception e) {
			LOGGER.debug("Could not evaluate classifier {} due to {}.", tsRefClassifier.toString(), e.getMessage());
			throw new EvaluationException("Could not evaluate classifier " + tsRefClassifier.toString(), e);
		}
		final long refEvaluationEnd = System.currentTimeMillis();
		final double refAcc = refEvaluation.pctCorrect() / 100d;

		LOGGER.debug("Finished evaluation of reference classifier. Took {} ms. Accuracy: {}",
				(refEvaluationEnd - refTimeStart), refAcc);
		result.put("ref_eval_time", (refEvaluationEnd - refTimeStart));
		result.put("ref_accuracy", refAcc);
	}

	/**
	 * Trains and evaluates a given <code>sfa.classification.Classifier</code>
	 * object with regard to the accuracy score using the given data from file
	 * <code>arffFile</code> partitioned into a fraction of
	 * <code>trainingPortion</code> as training data.
	 * 
	 * @param tsRefClassifier
	 *            Time series classifier reference to be trained and evaluated
	 * @param tsRefClassifierParams
	 *            Textual description of the time series classifier reference
	 *            parameters to be stored in the database
	 * @param result
	 *            Map used to store the database entry's information
	 * @param train
	 *            Training dataset
	 * @param test
	 *            Test dataset
	 */
	protected static void trainAndEvaluateSFARefClassifier(final sfa.classification.Classifier tsRefClassifier,
			final String tsRefClassifierParams, final Map<String, Object> result, final TimeSeries[] train,
			final TimeSeries[] test) {

		result.put("ref_classifier", tsRefClassifier.getClass().getSimpleName());
		result.put("ref_classifier_params", tsRefClassifierParams);

		// Training
		LOGGER.debug("Starting training of reference classifier...");
		long refTimeStart = System.currentTimeMillis();
		tsRefClassifier.fit(train);
		final long refTrainingEnd = System.currentTimeMillis();
		LOGGER.debug("Finished training of reference classifier. Took {} ms.", (refTrainingEnd - refTimeStart));
		result.put("ref_train_time", (refTrainingEnd - refTimeStart));

		// Evaluation
		LOGGER.debug("Starting evaluation of reference classifier...");
		refTimeStart = System.currentTimeMillis();
		Predictions preds = tsRefClassifier.score(test);
		double accuracy = preds.correct.get() / preds.labels.length;
		final long refEvaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of reference classifier. Took {} ms. Accuracy: {}",
				(refEvaluationEnd - refTimeStart), accuracy);
		result.put("ref_eval_time", (refEvaluationEnd - refTimeStart));
		result.put("ref_accuracy", accuracy);
	}

	/**
	 * Loads a time series dataset from an arbitrary number of arff files (multiple
	 * will be handled as multivariate dataset).
	 * 
	 * @param arffFiles
	 *            Arff files to be loaded
	 * @return Returns a TimeSeriesDataset object storing the arff data
	 * @throws TimeSeriesLoadingException
	 *             Thrown if the dataset could not be parsed
	 */
	private static TimeSeriesDataset loadDatasetFromArffFiles(final File... arffFiles)
			throws TimeSeriesLoadingException {
		TimeSeriesDataset dataset;
		try {
			if (arffFiles.length < 1)
				throw new IllegalArgumentException("At least one arff file must be given!");
			else if (arffFiles.length == 1) {
				dataset = TimeSeriesLoader.loadArff(arffFiles[0]);
			} else {
				dataset = TimeSeriesLoader.loadArffs(arffFiles);
			}
		} catch (TimeSeriesLoadingException e) {
			throw new TimeSeriesLoadingException("Could not load training dataset.", e);
		}
		return dataset;
	}

	/**
	 * Concatenates the names of the given files, separated by semicolons.
	 * 
	 * @param files
	 *            The file names to be concatenated.
	 * @return Returns the concatenated file namens or "none" if the given file
	 *         array is empty
	 */
	protected static String reduceFileNames(final File... files) {
		return Arrays.stream(files).map(file -> file.getName()).reduce((s1, s2) -> s1 + ";" + s2).orElse("none");
	}
}