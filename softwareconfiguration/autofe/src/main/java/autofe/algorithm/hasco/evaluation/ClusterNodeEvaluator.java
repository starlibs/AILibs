package autofe.algorithm.hasco.evaluation;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
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
	public Double f(final ILabeledPath<TFDNode, String> path) throws PathEvaluationException {
		if (this.hasPathEmptyParent(path)) {
			return null;
		}

		// If pipeline is too deep, assign worst value
		if (this.hasPathExceededPipelineSize(path)) {
			return MAX_EVAL_VALUE;
		}

		FilterPipeline pipe = this.extractPipelineFromNode(path);

		if (pipe != null && pipe.getFilters() != null) {
			// If pipeline is too deep, assign worst value
			if (pipe.getFilters().getItems().size() > this.maxPipelineSize) {
				return MAX_EVAL_VALUE;
			}

			try {
				double finalScore = Math.min(1 - EvaluationUtils.performClustering(pipe, this.data) + ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances()), MAX_EVAL_VALUE - 1);
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
