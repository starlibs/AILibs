package autofe.experiments;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aeonbits.owner.ConfigCache;

import autofe.util.EvaluationUtils;
import autofe.util.FileUtils;
import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import weka.core.Instances;

public class RankingExperimentEvaluator implements IExperimentSetEvaluator {

	private final IRankingConfig config = ConfigCache.getOrCreate(IRankingConfig.class);

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

		// int seed = Integer.valueOf(description.get("seed"));
		String dataSet = description.get("dataset");

		// Load data set
		List<Instances> dataSets = FileUtils.readInstances(dataSetFolder, dataSet, "_ranking.txt");
		double[] priorRanking = FileUtils
				.readDoubleArrayFromFile(dataSetFolder + File.separator + dataSet + "_ranking.txt", ",");

		// Calculate ranking scores for all data sets
		double[] scores = new double[dataSets.size()];
		for (int i = 0; i < dataSets.size(); i++) {
			scores[i] = benchmarkFunction.apply(dataSets.get(i));
		}

		// Calculate Kendall's Tau result
		double kendallsTau = EvaluationUtils.rankKendallsTau(priorRanking, scores);

		results.put("kendallsTau", kendallsTau);
		processor.processResults(results);

	}
}
