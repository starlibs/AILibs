package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.dataset.IDataset;
import sfa.classification.Classifier.Predictions;
import sfa.timeseries.TimeSeries;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class TSClassifierTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSClassifierTest.class);

	// For Bagnall reference repository
	// TODO: Distinguish between exceptions
	public static void compareClassifier(final Classifier tsRefClassifier, final TSClassifier<?> tsClassifier,
			final File arffFile, final int seed, final double trainingPortion, final String tsRefClassifierParams,
			final String tsClassifierParams) throws Exception {

		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", arffFile.getName());

		trainAndEvaluateRefClassifier(tsRefClassifier, arffFile, seed, trainingPortion, tsRefClassifierParams, result);
		trainAndEvaluateClassifier(tsClassifier, arffFile, seed, trainingPortion, tsClassifierParams, result);

		// TODO: Write results into database
	}

	// For SFA reference repository
	public static void compareClassifier(final sfa.classification.Classifier tsRefClassifier,
			final TSClassifier tsClassifier, final File arffFile, final int seed, final double trainingPortion,
			final String tsRefClassifierParams, final String tsClassifierParams) throws Exception {
		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", arffFile.getName());

		trainAndEvaluateRefClassifier(tsRefClassifier, arffFile, seed, trainingPortion, tsRefClassifierParams, result);
		trainAndEvaluateClassifier(tsClassifier, arffFile, seed, trainingPortion, tsClassifierParams, result);

		// TODO: Write results into database
	}

	private static void trainAndEvaluateClassifier(final TSClassifier tsClassifier, final File arffFile, final int seed,
			final double trainingPortion, final String tsClassifierParams, final Map<String, Object> result)
			throws Exception {

		result.put("classifier", tsClassifier.getClass().getSimpleName());
		result.put("classifier_params", tsClassifierParams);

		// TODO: Load dataset
		IDataset train = null;
		IDataset test = null;

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

		// TODO: Evaluate

		final long evaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of classifier. Took {} ms. Accuracy: {}", (evaluationEnd - timeStart), null);
		result.put("ref_eval_time", (evaluationEnd - timeStart));
		result.put("ref_accuracy", null);
	}

	private static void trainAndEvaluateRefClassifier(final Classifier tsRefClassifier, final File arffFile,
			final int seed, final double trainingPortion, final String tsRefClassifierParams,
			final Map<String, Object> result) throws Exception {

		result.put("ref_classifier", tsRefClassifier.getClass().getSimpleName());
		result.put("ref_classifier_params", tsRefClassifierParams);

		// Transform and split data to Weka instances
		ArffReader arffReader = new ArffReader(new FileReader(arffFile));
		final Instances wekaInstances = arffReader.getData();
		wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(wekaInstances, new Random(seed), trainingPortion);

		// Training
		LOGGER.debug("Starting training of reference classifier...");
		long refTimeStart = System.currentTimeMillis();
		tsRefClassifier.buildClassifier(split.get(0));
		final long refTrainingEnd = System.currentTimeMillis();
		LOGGER.debug("Finished training of reference classifier. Took {} ms.", (refTrainingEnd - refTimeStart));
		result.put("ref_train_time", (refTrainingEnd - refTimeStart));

		// Evaluation
		LOGGER.debug("Starting evaluation of reference classifier...");
		refTimeStart = System.currentTimeMillis();
		Evaluation refEvaluation = new Evaluation(split.get(0));
		refEvaluation.evaluateModel(tsRefClassifier, split.get(1));
		final long refEvaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of reference classifier. Took {} ms. Accuracy: {}",
				(refEvaluationEnd - refTimeStart), refEvaluation.pctCorrect());
		result.put("ref_eval_time", (refEvaluationEnd - refTimeStart));
		result.put("ref_accuracy", refEvaluation.pctCorrect());
	}

	private static void trainAndEvaluateRefClassifier(final sfa.classification.Classifier tsRefClassifier,
			final File arffFile, final int seed, final double trainingPortion, final String tsRefClassifierParams,
			final Map<String, Object> result) {

		result.put("ref_classifier", tsRefClassifier.getClass().getSimpleName());
		result.put("ref_classifier_params", tsRefClassifierParams);

		// Transform and split data
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
}