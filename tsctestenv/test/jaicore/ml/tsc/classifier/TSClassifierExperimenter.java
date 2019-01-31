package jaicore.ml.tsc.classifier;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;
import jaicore.basic.TimeOut;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.concurrent.TimeoutTimer;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;

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
		TimeOut timeout = new TimeOut(timeoutInS, TimeUnit.SECONDS);

		LOGGER.info("Load dataset...");
		String dataset = experiment.get("dataset");

		Pair<ASimplifiedTSClassifier<Integer>, Object> classifierPair = SimplifiedTSClassifierTest
				.createClassifierPairsWithDefaultParameter(experiment.get("algorithm"), (int) seed, timeout);
		ASimplifiedTSClassifier<Integer> ownClassifier = classifierPair.getX();
		Object refClassifier = classifierPair.getY();

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
