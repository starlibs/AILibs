package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.EvaluationUtils;
import hasco.exceptions.ComponentInstantiationFailedException;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.model.travesaltree.Node;

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
		if (node.getParent() == null) {
			return 0.0;
		}

		FilterPipeline pipe;
		try {
			pipe = getPipelineFromNode(node);
		} catch (ComponentInstantiationFailedException e1) {
			throw new NodeEvaluationException(e1, "Could not evaluate pipeline");
		}

		if (pipe != null && pipe.getFilters() != null) {
			// If pipeline is too deep, assign worst value
			if (pipe.getFilters().getItems().size() > maxPipelineSize) {
				return MAX_EVAL_VALUE;
			}

			try {
				double finalScore = Math.min(1 - EvaluationUtils.performClustering(pipe, data) + ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(data.getInstances()), MAX_EVAL_VALUE - 1);
				logger.debug("Final clustering node evaluation score: " + finalScore);
				return finalScore;
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("Could not evaluate pipeline. Reason: " + e.getMessage());
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
