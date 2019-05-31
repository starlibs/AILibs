package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.model.travesaltree.Node;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.EvaluationUtils;

/**
 * Evaluator used for node evaluation to guide the search using a simple clustering benchmark function.
 *
 * @author Julian Lienen, wever
 *
 */
public class ClusterNodeEvaluator extends AbstractHASCOFENodeEvaluator {
	public ClusterNodeEvaluator(final int maxPipelineSize) {
		super(maxPipelineSize);
	}

	private static final Logger logger = LoggerFactory.getLogger(ClusterNodeEvaluator.class);

	@Override
	public Double f(final Node<TFDNode, ?> node) throws NodeEvaluationException {
		if(hasNodeEmptyParent(node))
			return null;

		// If pipeline is too deep, assign worst value
		if (hasPathExceededPipelineSize(node)) {
			return MAX_EVAL_VALUE;
		}

		FilterPipeline pipe = extractPipelineFromNode(node);

		if (pipe != null && pipe.getFilters() != null) {
			// If pipeline is too deep, assign worst value
			if (pipe.getFilters().getItems().size() > maxPipelineSize) {
				return MAX_EVAL_VALUE;
			}

			try {
				double finalScore = Math.min(1 - EvaluationUtils.performClustering(pipe, data) + ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(data.getInstances()), MAX_EVAL_VALUE - 1);
				logger.debug("Final clustering node evaluation score: {}", finalScore);
				return finalScore;
			} catch (Exception e) {
				logger.warn("Could not evaluate pipeline. Reason: {}", e.getMessage());
				return null;
			}
		} else if (pipe == null) {
			logger.debug("Null pipe");
			return 0.0;
		} else {
			logger.debug("Found a non-working pipeline.");
			return 0.0;
		}
	}
}
