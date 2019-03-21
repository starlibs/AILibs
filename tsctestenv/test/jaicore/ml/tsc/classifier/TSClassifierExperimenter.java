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
import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;
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
	private final IMultiClassClassificationExperimentConfig config;

	public TSClassifierExperimenter(final IMultiClassClassificationExperimentConfig config) {
		this.config = config;
	}

	@Override
	public IExperimentSetConfig getConfig() {
		return this.config;
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

		// Initialize and parameterize classifiers
		Pair<ASimplifiedTSClassifier<Integer>, Object> classifierPair = SimplifiedTSClassifierTest
				.createClassifierPairsWithSpecificParameter(experiment, timeout);

		ASimplifiedTSClassifier<Integer> ownClassifier = classifierPair.getX();
		Object refClassifier = classifierPair.getY();

		String datasetPathPrefix = this.config.getDatasetFolder().getAbsolutePath() + "/" + dataset + "/" + dataset;

		try {
			Map<String, Object> results = SimplifiedTSClassifierTest.compareClassifiers(refClassifier, ownClassifier,
					(int) seed, "", "", new File(datasetPathPrefix + "_TRAIN.arff"),
					new File(datasetPathPrefix + "_TEST.arff"));
			processor.processResults(results);

			LOGGER.info("Evaluation of experiment with id {} finished.", experimentEntry.getId());
		} catch (TimeSeriesLoadingException e) {
			LOGGER.error("Could not finish experiment due to {}.", e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		String algorithm = "tsbf";
		int numRuns = 5;
		
		if(args.length == 2) {
			algorithm = args[0];
			numRuns = Integer.parseInt(args[1]);
		}
		
		IMultiClassClassificationExperimentConfig config = null;
		switch (algorithm) {
		case "ls":
			config = ConfigCache.getOrCreate(LearnShapeletsExperimentConfig.class);
			break;
		case "st":
			config = ConfigCache.getOrCreate(ShapeletTransformExperimentConfig.class);
			break;
		case "tsf":
			config = ConfigCache.getOrCreate(TimeSeriesForestExperimentConfig.class);
			break;
		case "tsbf":
			config = ConfigCache.getOrCreate(TimeSeriesBagOfFeaturesExperimentConfig.class);
			break;
		case "lps":
			config = ConfigCache.getOrCreate(LearnPatternSimilarityExperimentConfig.class);
			break;
		default:
			config = ConfigCache.getOrCreate(LearnShapeletsExperimentConfig.class);
		}
		
		ExperimentRunner runner = new ExperimentRunner(new TSClassifierExperimenter(config));

		runner.randomlyConductExperiments(numRuns, true);
		TimeoutTimer.getInstance().stop();
		LOGGER.info("Experiment runner is shutting down.");
		System.exit(0);
	}

}
