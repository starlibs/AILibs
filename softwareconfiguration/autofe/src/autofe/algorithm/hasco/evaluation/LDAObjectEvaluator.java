package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

public class LDAObjectEvaluator extends AbstractHASCOFEObjectEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(LDAObjectEvaluator.class);

	@Override
	public Double evaluate(FilterPipeline pipeline) throws Exception {
		logger.debug("Applying and evaluating pipeline " + pipeline.toString());
		DataSet dataSet = pipeline.applyFilter(this.data, true);

		final double ldaScore = EvaluationUtils.performLDA(dataSet.getInstances());

		logger.debug("LDA object evaluator score: " + ldaScore);
		return 1 - ldaScore
				+ ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances());
	}

}
