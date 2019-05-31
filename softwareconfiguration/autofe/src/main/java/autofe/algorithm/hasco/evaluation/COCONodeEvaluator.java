package autofe.algorithm.hasco.evaluation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.model.travesaltree.Node;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;
import weka.core.Instances;

/**
 * Evaluator using the congenerous cosine distance (COCO) by Liu et. al., 2017 (cf. https://arxiv.org/pdf/1710.00870.pdf).
 *
 * @author Julian Lienen
 *
 */
public class COCONodeEvaluator extends AbstractHASCOFENodeEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(COCONodeEvaluator.class);

	public COCONodeEvaluator(final int maxPipelineSize) {
		super(maxPipelineSize);
	}

	@Override
	public Double f(final Node<TFDNode, ?> node) throws NodeEvaluationException  {
		if(hasNodeEmptyParent(node))
			return null;

		// If pipeline is too deep, assign worst value
		if (hasPathExceededPipelineSize(node)) {
			return MAX_EVAL_VALUE;
		}

		FilterPipeline pipe = extractPipelineFromNode(node);
		if (pipe != null && pipe.getFilters() != null) {
			try {
				logger.debug("Applying and evaluating pipeline {}.", pipe);
				DataSet dataSet = pipe.applyFilter(data, true);

				// Get small batch
				List<Instances> split = WekaUtil.getStratifiedSplit(dataSet.getInstances(), 42, 0.01d);
				Instances insts = split.get(0);

				double loss = (-1) * EvaluationUtils.calculateCOCOForBatch(insts);

				logger.debug("COCO node evaluation score: {}", loss);
				return loss;
			} catch (Exception e) {
				logger.warn("Got exception.", e);
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
