package jaicore.ml.tsc.classifier;

import java.io.File;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SQLAdapter;
import jaicore.concurrent.TimeoutTimer;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.quality_measures.FStat;
import timeseriesweka.classifiers.ShapeletTransformClassifier;

public class TSClassifierExperimenter implements IExperimentSetEvaluator {
	private static final Logger LOGGER = LoggerFactory.getLogger(TSClassifierExperimenter.class);

	private static final TSClassifierExperimentConfig CONFIG = ConfigCache
			.getOrCreate(TSClassifierExperimentConfig.class);

	@Override
	public IExperimentSetConfig getConfig() {
		return CONFIG;
	}

	@Override
	public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
			IExperimentIntermediateResultProcessor processor) throws Exception {
		// TODO Auto-generated method stub
		
		Map<String, String> experiment = experimentEntry.getExperiment().getValuesOfKeyFields();
		LOGGER.info("Evaluate experiment: {}", experiment);

		long seed = Long.valueOf(experiment.get("seed"));
		long timeoutInS = Long.valueOf(experiment.get("timeout"));
		
		LOGGER.info("Load dataset...");
		String dataset = experiment.get("dataset");
		
		Object refClassifier = null;
		ASimplifiedTSClassifier<Integer> ownClassifier = null;
		
		if(experiment.get("algorithm").equals("ShapeletTransform")) {
			
			int k= 205;
			final int minShapeletLength = 3;
			final int maxShapeletLength = 23;
			
			refClassifier = new ShapeletTransformClassifier();
			((ShapeletTransformClassifier) refClassifier).setSeed(seed);
			((ShapeletTransformClassifier) refClassifier).setNumberOfShapelets(k);
			
			ownClassifier = new ShapeletTransformTSClassifier(k, new FStat(), (int) seed, false, minShapeletLength, maxShapeletLength);
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
