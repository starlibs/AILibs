package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;
import hasco.exceptions.ComponentInstantiationFailedException;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.model.travesaltree.Node;

public class EnsembleNodeEvaluator extends AbstractHASCOFENodeEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(EnsembleNodeEvaluator.class);

	public EnsembleNodeEvaluator(final int maxPipelineSize) {
		super(maxPipelineSize);
	}

	@Override
	public Double f(final Node<TFDNode, ?> node) throws NodeEvaluationException {
		if (node.getParent() == null) {
			return null;
		}

		// If pipeline is too deep, assign worst value
		if (node.path().size() > maxPipelineSize) {
			return MAX_EVAL_VALUE;
		}

		FilterPipeline pipe;
		try {
			pipe = getPipelineFromNode(node);
		} catch (ComponentInstantiationFailedException e1) {
			throw new NodeEvaluationException(e1, "Could not evaluate pipeline.");
		}

		if (pipe != null && pipe.getFilters() != null) {
			try {
				logger.debug("Applying and evaluating pipeline " + pipe.toString());
				DataSet dataSet = pipe.applyFilter(data, true);
				logger.debug("Applied pipeline. Starting benchmarking...");

				double ensembleScore = EvaluationUtils.performEnsemble(dataSet.getInstances());
				double finalScore = Math.min(1 - ensembleScore + ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(data.getInstances()), MAX_EVAL_VALUE - 1);

				logger.debug("Ensemble benchmark result: " + finalScore);

				return finalScore;

			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("Could not evaluate pipeline. Reason: " + e.getMessage());
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
