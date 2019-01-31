package jaicore.ml.tsc.classifier;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;
import jaicore.basic.TimeOut;
import jaicore.concurrent.TimeoutTimer;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.quality_measures.FStat;
import timeseriesweka.classifiers.LearnShapelets;
import timeseriesweka.classifiers.ShapeletTransformClassifier;

/**
 * Experimenter class which can randomly conduct experiments and storing the
 * evaluation results in a specified database.
 * 
 * @author Julian Lienen
 *
 */
public class TSClassifierExperimenter implements IExperimentSetEvaluator {
	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TSClassifierExperimenter.class);

	/**
	 * The experiment configuration
	 */
	private static final TSClassifierExperimentConfig CONFIG = ConfigCache
			.getOrCreate(TSClassifierExperimentConfig.class);

	@Override
	public IExperimentSetConfig getConfig() {
		return CONFIG;
	}

	@Override
	public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
			IExperimentIntermediateResultProcessor processor) throws Exception {

		Map<String, String> experiment = experimentEntry.getExperiment().getValuesOfKeyFields();
		LOGGER.info("Evaluate experiment: {}", experiment);

		long seed = Long.valueOf(experiment.get("seed"));
		long timeoutInS = Long.valueOf(experiment.get("timeout"));

		LOGGER.info("Load dataset...");
		String dataset = experiment.get("dataset");

		Object refClassifier = null;
		ASimplifiedTSClassifier<Integer> ownClassifier = null;

		switch (experiment.get("algorithm")) {
		case "ShapeletTransform":
			int k = 205;
			final int minShapeletLength = 3;
			final int maxShapeletLength = 23;

			refClassifier = new ShapeletTransformClassifier();
			((ShapeletTransformClassifier) refClassifier).setSeed(seed);
			((ShapeletTransformClassifier) refClassifier).setNumberOfShapelets(k);

			ownClassifier = new ShapeletTransformTSClassifier(k, new FStat(), (int) seed, false, minShapeletLength,
					maxShapeletLength, true, new TimeOut(timeoutInS, TimeUnit.SECONDS));
			break;

		case "LearnShapelets":
			// Initialize classifiers with values selected by reference classifier by
			// default
			int K = 8;
			double learningRate = 0.1;
			double regularization = 0.01;
			int scaleR = 3;
			double minShapeLength = 0.2;
			int maxIter = 600;

			ownClassifier = new LearnShapeletsClassifier(K, learningRate, regularization, scaleR, minShapeLength,
					maxIter, (int) seed);

			refClassifier = new LearnShapelets();
			((LearnShapelets) refClassifier).setSeed(seed);
			((LearnShapelets) refClassifier).fixParameters();
			break;
		case "TimeSeriesForest":
			// TODO
			break;
		default:
			LOGGER.error("Please specify a valid algorithm. An invalid value was given: {}",
					experiment.get("algorithm"));
			return;
		}

		try {
			Map<String, Object> results = SimplifiedTSClassifierTest.compareClassifiers(refClassifier, ownClassifier,
					(int) seed, null, null,
					new File(CONFIG.getDatasetFolder().getAbsolutePath() + "\\" + dataset + "\\" + dataset
							+ "_TRAIN.arff"),
					new File(CONFIG.getDatasetFolder().getAbsolutePath() + "\\" + dataset + "\\" + dataset
							+ "_TEST.arff"));
			processor.processResults(results);

			LOGGER.info("Evaluation of experiment with id {} finished.", experimentEntry.getId());
		} catch (TimeSeriesLoadingException e) {
			LOGGER.error("Could not finish experiment due to {}.", e.getMessage());
		}
	}

	public static void main(final String[] args) {
		ExperimentRunner runner = new ExperimentRunner(new TSClassifierExperimenter());
		runner.randomlyConductExperiments(1, true);
		TimeoutTimer.getInstance().stop();
		LOGGER.info("Experiment runner is shutting down.");
		System.exit(0);
	}

}
