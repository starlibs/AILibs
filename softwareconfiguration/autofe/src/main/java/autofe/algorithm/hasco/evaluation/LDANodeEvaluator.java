package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.model.travesaltree.Node;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

public class LDANodeEvaluator extends AbstractHASCOFENodeEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(LDANodeEvaluator.class);

    public LDANodeEvaluator(final int maxPipelineSize) {
        super(maxPipelineSize);
    }

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
            try {
                logger.debug("Applying and evaluating pipeline {}", pipe);
                DataSet dataSet = pipe.applyFilter(data, true);

                double ldaScore = EvaluationUtils.performLDA(dataSet.getInstances());

                double finalScore = Math.min(1 - ldaScore + ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(data.getInstances()), MAX_EVAL_VALUE - 1);

                logger.debug("Final LDA score: {}", finalScore);
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
