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
import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.TimeSeriesLoader;
import jaicore.ml.tsc.util.TimeSeriesUtil;
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
	 * implementations).
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
	 */
	public static Map<String, Object> compareClassifier(final Classifier tsRefClassifier,
			final TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> tsClassifier,
			final int seed,
			final double trainingPortion, final String tsRefClassifierParams, final String tsClassifierParams,
			final File... arffFiles)
			throws FileNotFoundException, EvaluationException, TrainingException, IOException, PredictionException {

		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", reduceFileNames(arffFiles));

		trainAndEvaluateRefClassifier(tsRefClassifier, seed, trainingPortion, tsRefClassifierParams, result, arffFiles);
		trainAndEvaluateClassifier(tsClassifier, seed, trainingPortion, tsClassifierParams, result, arffFiles);

		return result;
	}

	// For SFA reference repository
	/**
	 * Function comparing a time series classifier deriving from the
	 * <code>sfa.classification.Classifier</code> interface (used in Schaefer's
	 * reference implementations).
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
	 *            (range 0 to 1)
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
	 * @throws TrainingException
	 *             Will be thrown if the given classifier could not be trained
	 * @throws PredictionException
	 */
	public static Map<String, Object> compareClassifier(
			final sfa.classification.Classifier tsRefClassifier,
			final TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> tsClassifier, final int seed,
			final double trainingPortion, final String tsRefClassifierParams, final String tsClassifierParams,
			final File... arffFiles) throws TrainingException, PredictionException {
		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", reduceFileNames(arffFiles));

		trainAndEvaluateRefClassifier(tsRefClassifier, seed, trainingPortion, tsRefClassifierParams, result, arffFiles);
		trainAndEvaluateClassifier(tsClassifier, seed, trainingPortion, tsClassifierParams, result, arffFiles);

		return result;
	}

	/**
	 * Trains and evaluates a given <code>TSClassifier</code> object with regard to
	 * the accuracy score using the given data from file <code>arffFile</code>
	 * partitioned into a fraction of <code>trainingPortion</code> as training data.
	 * 
	 * @param tsClassifier
	 *            Time series classifier to be trained and evaluated
	 * 
	 * @param seed
	 *            Seed used for randomized splitting the given data into train and
	 *            test set
	 * @param trainingPortion
	 *            Portion of the data used for training (range from 0 to 1)
	 * @param tsClassifierParams
	 *            Textual description of the time series classifier parameters to be
	 *            stored in the database
	 * @param result
	 *            Map used to store the database entry's information
	 * @param arffFiles
	 *            Arff files containing the data used for training and evaluation
	 *            (assumes univariate dataset, if only one file is given)
	 * @throws TrainingException
	 *             Will be thrown if training of <code>tsClassifier</code> fails
	 * @throws PredictionException 
	 */
	private static void trainAndEvaluateClassifier(
			final TSClassifier<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset> tsClassifier, final int seed,
			final double trainingPortion, final String tsClassifierParams, final Map<String, Object> result,
			final File... arffFiles) throws TrainingException, PredictionException {

		result.put("classifier", tsClassifier.getClass().getSimpleName());
		result.put("classifier_params", tsClassifierParams);

		// Load dataset
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
			throw new TrainingException("Could not load training dataset.", e);
		}

		Pair<TimeSeriesDataset, TimeSeriesDataset> trainTest = TimeSeriesUtil.getStratifiedSplit(dataset,
				trainingPortion);
		TimeSeriesDataset train = trainTest.getX();
		TimeSeriesDataset test = trainTest.getY();

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

		CategoricalAttributeType targetType = tsClassifier.getTargetType();
		List<CategoricalAttributeValue> predictions = tsClassifier.predict(test);
		int totalPreds = predictions.size();
		int correct = 0;
		for (int i=0; i<totalPreds; i++) {
			CategoricalAttributeValue prediction = predictions.get(i);
			if (targetType.getDomain().indexOf(prediction.getValue()) == test.getTargets().getInt(i))
				correct++;
		}
			
		double accuracy = (double) correct / totalPreds;

		final long evaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of classifier. Took {} ms. Accuracy: {}", (evaluationEnd - timeStart), accuracy);
		result.put("ref_eval_time", (evaluationEnd - timeStart));
		result.put("ref_accuracy", accuracy);
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
	 * @param trainingPortion
	 *            Portion of the data used for training (range from 0 to 1)
	 * @param tsRefClassifierParams
	 *            Textual description of the time series classifier reference
	 *            parameters to be stored in the database
	 * @param result
	 *            Map used to store the database entry's information
	 * @param arffFiles
	 *            Arff files containing the data used for training and evaluation
	 *            (assumes univariate dataset, if only one file is given)
	 * @throws IOException
	 *             Will be thrown if the data could not be read
	 * @throws FileNotFoundException
	 *             Will be thrown if the given data set file could not be found
	 * @throws EvaluationException
	 *             Will be thrown if the given classifier could not be evaluated
	 * @throws TrainingException
	 *             Will be thrown if the given classifier could not be trained
	 */
	private static void trainAndEvaluateRefClassifier(final Classifier tsRefClassifier, final int seed,
			final double trainingPortion, final String tsRefClassifierParams, final Map<String, Object> result,
			final File... arffFiles) throws FileNotFoundException, IOException, EvaluationException, TrainingException {

		result.put("ref_classifier", tsRefClassifier.getClass().getSimpleName());
		result.put("ref_classifier_params", tsRefClassifierParams);

		// Transform and split data to Weka instances
		if (arffFiles == null || arffFiles.length < 1)
			throw new IllegalArgumentException("At least one arff file must be given!");
		if (arffFiles.length > 1) {
			// TODO: Support multivariate evaluation
			throw new UnsupportedOperationException("Multivariate ref ts classifier evaluation is not supported yet.");
		}
		ArffReader arffReader = new ArffReader(new FileReader(arffFiles[0]));
		final Instances wekaInstances = arffReader.getData();
		wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, seed, trainingPortion);

		// Training
		LOGGER.debug("Starting training of reference classifier...");
		long refTimeStart = System.currentTimeMillis();
		try {
			tsRefClassifier.buildClassifier(split.get(0));
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
			refEvaluation = new Evaluation(split.get(0));
			refEvaluation.evaluateModel(tsRefClassifier, split.get(1));
		} catch (Exception e) {
			LOGGER.debug("Could not evaluate classifier {} due to {}.", tsRefClassifier.toString(), e.getMessage());
			throw new EvaluationException("Could not evaluate classifier " + tsRefClassifier.toString(), e);
		}
		final long refEvaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of reference classifier. Took {} ms. Accuracy: {}",
				(refEvaluationEnd - refTimeStart), refEvaluation.pctCorrect());
		result.put("ref_eval_time", (refEvaluationEnd - refTimeStart));
		result.put("ref_accuracy", refEvaluation.pctCorrect());
	}

	/**
	 * Trains and evaluates a given <code>sfa.classification.Classifier</code>
	 * object with regard to the accuracy score using the given data from file
	 * <code>arffFile</code> partitioned into a fraction of
	 * <code>trainingPortion</code> as training data.
	 * 
	 * @param tsRefClassifier
	 *            Time series classifier reference to be trained and evaluated
	 * @param seed
	 *            Seed used for randomized splitting the given data into train and
	 *            test set
	 * @param trainingPortion
	 *            Portion of the data used for training (range from 0 to 1)
	 * @param tsRefClassifierParams
	 *            Textual description of the time series classifier reference
	 *            parameters to be stored in the database
	 * @param result
	 *            Map used to store the database entry's information
	 * @param arffFiles
	 *            Arff files containing the data used for training and evaluation
	 *            (assumes univariate dataset, if only one file is given)
	 */
	private static void trainAndEvaluateRefClassifier(final sfa.classification.Classifier tsRefClassifier,
			final int seed, final double trainingPortion, final String tsRefClassifierParams,
			final Map<String, Object> result, final File... arffFiles) {

		result.put("ref_classifier", tsRefClassifier.getClass().getSimpleName());
		result.put("ref_classifier_params", tsRefClassifierParams);

		// Transform and split data
		// TODO
		TimeSeries[] train = null;
		TimeSeries[] test = null;

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
	 * Concatenates the names of the given files, separated by semicolons.
	 * 
	 * @param files
	 *            The file names to be concatenated.
	 * @return Returns the concatenated file namens or "none" if the given file
	 *         array is empty
	 */
	private static String reduceFileNames(final File... files) {
		return Arrays.stream(files).map(file -> file.getName()).reduce((s1, s2) -> s1 + ";" + s2).orElse("none");
	}
}