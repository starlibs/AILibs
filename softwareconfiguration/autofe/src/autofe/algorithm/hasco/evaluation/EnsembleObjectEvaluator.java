package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

public class EnsembleObjectEvaluator extends AbstractHASCOFEObjectEvaluator {
	private static final Logger logger = LoggerFactory.getLogger(EnsembleObjectEvaluator.class);

	@Override
	public Double evaluate(FilterPipeline pipeline) throws Exception {
		logger.debug("Applying and evaluating pipeline " + pipeline.toString());
		DataSet dataSet = pipeline.applyFilter(this.data, true);

		logger.debug("Applied pipeline. Starting benchmarking...");

		double ensembleScore = EvaluationUtils.performEnsemble(dataSet.getInstances());
		double finalScore = ensembleScore
				+ ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances());

		logger.debug("Ensemble benchmark result: " + finalScore);

		return finalScore;
	}
}
