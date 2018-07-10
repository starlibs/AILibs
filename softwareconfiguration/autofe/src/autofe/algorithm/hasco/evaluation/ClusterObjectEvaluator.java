package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
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
	public Double evaluate(FilterPipeline object) throws Exception {
		double finalScore = EvaluationUtils.performClustering(object, this.data)
				+ ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances());
		logger.debug("Cluster object evaluator final score: " + finalScore);
		return finalScore;
	}
}
