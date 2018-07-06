package autofe.algorithm.hasco.evaluation;

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

	// private static final Logger logger =
	// LoggerFactory.getLogger(ClusterObjectEvaluator.class);

	@Override
	public Double evaluate(FilterPipeline object) throws Exception {

		return EvaluationUtils.performClustering(object, this.data);
	}
}
