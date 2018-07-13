package autofe.experiments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aeonbits.owner.ConfigCache;

import autofe.util.EvaluationUtils;
import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import weka.core.Instances;

public class RankingExperimentEvaluator implements IExperimentSetEvaluator {

	private final IRankingConfig config = ConfigCache.getOrCreate(IRankingConfig.class);

	private final double[] priorRanking;
	private final List<Instances> dataSets;

	public RankingExperimentEvaluator(final double[] priorRanking, final List<Instances> dataSets) {
		this.priorRanking = priorRanking;
		this.dataSets = dataSets;
	}

	@Override
	public IExperimentSetConfig getConfig() {
		return this.config;
	}

	@Override
	public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
			IExperimentIntermediateResultProcessor processor) throws Exception {

		Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();

		// Get benchmark function
		String benchmark = description.get("benchmark");
		Function<Instances, Double> benchmarkFunction = EvaluationUtils.getBenchmarkFuntionByName(benchmark);

		// TODO: Currently unused
		int seed = Integer.valueOf(description.get("seed"));

		Map<String, Object> results = new HashMap<>();

		// Calculate ranking scores for all data sets
		double[] scores = new double[this.dataSets.size()];
		for (int i = 0; i < this.dataSets.size(); i++) {
			scores[i] = benchmarkFunction.apply(this.dataSets.get(i));
		}

		// Calculate Kendall's Tau result
		double kendallsTau = EvaluationUtils.rankKendallsTau(this.priorRanking, scores);

		results.put("kendallsTau", kendallsTau);
		processor.processResults(results);

	}

}
