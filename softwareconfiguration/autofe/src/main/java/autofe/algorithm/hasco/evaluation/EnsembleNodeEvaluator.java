package autofe.algorithm.hasco.evaluation;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

public class EnsembleNodeEvaluator extends AbstractHASCOFENodeEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(EnsembleNodeEvaluator.class);

	public EnsembleNodeEvaluator(final int maxPipelineSize) {
		super(maxPipelineSize);
	}

	@Override
	public Double f(final ILabeledPath<TFDNode, String> path) throws PathEvaluationException {
		if(this.hasPathEmptyParent(path)) {
			return null;
		}

		// If pipeline is too deep, assign worst value
		if (this.hasPathExceededPipelineSize(path)) {
			return MAX_EVAL_VALUE;
		}

		FilterPipeline pipe = this.extractPipelineFromNode(path);

		if (pipe != null && pipe.getFilters() != null) {
			try {
				logger.debug("Applying and evaluating pipeline {}", pipe);
				DataSet dataSet = pipe.applyFilter(this.data, true);
				logger.debug("Applied pipeline. Starting benchmarking...");

				double ensembleScore = EvaluationUtils.performEnsemble(dataSet.getInstances());
				double finalScore = Math.min(1 - ensembleScore + ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances()), MAX_EVAL_VALUE - 1);

				logger.debug("Ensemble benchmark result: {}", finalScore);

				return finalScore;

			} catch (Exception e) {
				logger.warn("Could not evaluate pipeline. Reason: {}", e.getMessage());
				return null;
			}
		} else if (pipe == null) {
			logger.debug("Null pipe");
			return null;
		} else {
			logger.debug("Found a non-working pipeline.");
			return MAX_EVAL_VALUE;
		}
	}

}
