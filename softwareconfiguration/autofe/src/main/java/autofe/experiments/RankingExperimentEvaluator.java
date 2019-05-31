package autofe.experiments;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aeonbits.owner.ConfigCache;

import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import autofe.util.EvaluationUtils;
import autofe.util.FileUtils;
import weka.core.Instances;

public class RankingExperimentEvaluator implements IExperimentSetEvaluator {

	private final IRankingConfig config = ConfigCache.getOrCreate(IRankingConfig.class);

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) {

		String dataSetFolder = config.getDatasetFolder();
		if (dataSetFolder == null || !(new File(dataSetFolder).exists())) {
			throw new IllegalArgumentException("Data set folder must exist!");
		}

		Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();

		// Get benchmark function
		String benchmark = description.get("benchmark");
		Function<Instances, Double> benchmarkFunction = EvaluationUtils.getBenchmarkFunctionByName(benchmark);

		Map<String, Object> results = new HashMap<>();

		String dataSet = description.get("dataset");

		// Load data set
		List<Instances> dataSets = FileUtils.readInstances(dataSetFolder, dataSet, "_ranking.txt");
		double[] priorRanking = FileUtils.readDoubleArrayFromFile(dataSetFolder + File.separator + dataSet + "_ranking.txt", ",");

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
