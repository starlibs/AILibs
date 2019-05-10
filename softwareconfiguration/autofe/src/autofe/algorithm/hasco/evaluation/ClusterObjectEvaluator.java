package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

/**
 * Object evaluator used to evaluate full FilterPipeline objects using a simple
 * clustering approach.
 *
 * @author Julian Lienen
 *
 */
public class ClusterObjectEvaluator extends AbstractHASCOFEObjectEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ClusterObjectEvaluator.class);

	@Override
	public Double evaluate(final FilterPipeline pipeline) throws Exception {
		logger.debug("Evaluate filter pipeline {}", pipeline);
		long startTimestamp = System.currentTimeMillis();

		logger.info("Applying and evaluating pipeline " + pipeline.toString());
		DataSet dataSet = pipeline.applyFilter(this.data, false);

		double finalScore = EvaluationUtils.performKernelClustering(dataSet.getInstances(), 1)
				- ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances());
		logger.debug("Cluster object evaluator final score: " + finalScore);

		this.storeResult(pipeline, finalScore, (System.currentTimeMillis() - startTimestamp));
		return 1 - finalScore;
	}
}
