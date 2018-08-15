package autofe.experiments;

import java.io.File;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.util.EvaluationUtils;
import autofe.util.FileUtils;
import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import weka.core.Instances;

public class BenchmarkRankExperimentEvaluator implements IExperimentSetEvaluator {
	private static final Logger logger = LoggerFactory.getLogger(BenchmarkRankExperimentEvaluator.class);

	private final IBenchmarkRankConfig config = ConfigCache.getOrCreate(IBenchmarkRankConfig.class);

	// TODO: Change this parameter
	private static final int MAX_COUNT_VARIATIONS = 10;

	@Override
	public IExperimentSetConfig getConfig() {
		return this.config;
	}

	@Override
	public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
			IExperimentIntermediateResultProcessor processor) throws Exception {
		String dataSetFolder = this.config.getDatasetFolder();
		if (dataSetFolder == null || !(new File(dataSetFolder).exists()))
			throw new IllegalArgumentException("Data set folder must exist!");

		Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();

		// Get benchmark function
		String benchmark = description.get("benchmark");
		Function<Instances, Double> benchmarkFunction = EvaluationUtils.getBenchmarkFuntionByName(benchmark);

		Map<String, Object> results = new HashMap<>();

		int seed = Integer.valueOf(description.get("seed"));
		String dataSet = description.get("dataset");

		// Read prior ranking
		ResultSet mlPlanScores = adapter.getResultsOfQuery("SELECT score FROM mlplanranking WHERE seed = " + seed
				+ " and dataset = \"" + dataSet + "\" ORDER BY variation ASC");

		// Retrieve prior ranking from data base
		double[] priorRanking = new double[MAX_COUNT_VARIATIONS];
		for (int i = 0; i < priorRanking.length; i++) {
			mlPlanScores.next();
			double varScore = mlPlanScores.getDouble(1);
			// logger.debug("Var " + i + " score: " + varScore);
			priorRanking[i] = varScore;

		}
		logger.debug("Prior ranking: " + Arrays.toString(priorRanking));

		// Compute score
		double[] scores = new double[MAX_COUNT_VARIATIONS];
		for (int i = 0; i < priorRanking.length; i++) {
			String filePath = dataSetFolder + File.separator + dataSet + "_" + seed + "_" + i + ".arff";
			Instances variation = FileUtils.readSingleInstances(filePath);
			scores[i] = benchmarkFunction.apply(variation);
		}

		// Calculate Kendall's Tau result
		double kendallsTau = EvaluationUtils.rankKendallsTau(priorRanking, scores);

		results.put("kendallsTau", kendallsTau);
		processor.processResults(results);
	}
}
