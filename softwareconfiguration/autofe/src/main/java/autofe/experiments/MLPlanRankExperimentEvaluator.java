package autofe.experiments;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.HASCOFeatureEngineering;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import autofe.util.FileUtils;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import weka.core.Instances;

public class MLPlanRankExperimentEvaluator implements IExperimentSetEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(MLPlanRankExperimentEvaluator.class);

	private final IMLPlanRankConfig config = ConfigCache.getOrCreate(IMLPlanRankConfig.class);

	private static final int MAX_PIPELINE_SIZE = 10;
	private static final int DATASET_GENERATION_TIMEOUT = 600 * 1000; // 360 * 1000;
	private static final int MLPLAN_TIMEOUT_S = 3600; // 3600;
	private static final double MLPLAN_SPLIT_RATIO = 0.75;

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {

		try {
			String dataSetFolder = config.getDatasetFolder();
			if (dataSetFolder == null || !(new File(dataSetFolder).exists())) {
				throw new IllegalArgumentException("Data set folder must exist!");
			}

			Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();

			String variation = description.get("variation");
			String dataSet = description.get("dataset");
			int seed = Integer.valueOf(description.get("seed"));

			Map<String, Object> results = new HashMap<>();

			// Load data
			String filePath = dataSetFolder + File.separator + dataSet + "_" + seed + "_" + variation + ".arff";

			Instances dsInstances = FileUtils.readSingleInstances(filePath);
			if (dsInstances == null) {
				// Create random
				int dataSetID = DataSetUtils.getDataSetIDByName(dataSet);
				List<Instances> dataSetVariations = HASCOFeatureEngineering.generateRandomDataSets(dataSetID, 1, MAX_PIPELINE_SIZE, DATASET_GENERATION_TIMEOUT, seed);

				if (dataSetVariations.size() != 1) {
					throw new IllegalStateException("HASCOFE has not generated the expected amount of data set variations.");
				}

				dsInstances = dataSetVariations.get(0);

				// Save generated data set variation
				FileUtils.saveSingleInstances(dsInstances, filePath);
			}

			int numCPUs = Runtime.getRuntime().availableProcessors();
			double mlPlanScore = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT_S, dsInstances, MLPLAN_SPLIT_RATIO, seed, logger, false, numCPUs);

			results.put("score", mlPlanScore);
			processor.processResults(results);
		} catch (Exception e) {
			throw new ExperimentEvaluationFailedException(e);
		}
	}
}
