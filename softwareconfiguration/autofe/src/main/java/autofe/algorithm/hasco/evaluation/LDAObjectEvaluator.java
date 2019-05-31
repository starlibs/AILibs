package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

public class LDAObjectEvaluator extends AbstractHASCOFEObjectEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(LDAObjectEvaluator.class);

    @Override
    public Double evaluate(final FilterPipeline pipeline) throws InterruptedException, ObjectEvaluationFailedException {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null");
        }

        long startTimestamp = System.currentTimeMillis();
        logger.info("Applying and evaluating pipeline {}.", pipeline);

        DataSet dataSet = pipeline.applyFilter(data, false);

        logger.debug("Perform LDA");
        try {
            final double ldaScore = EvaluationUtils.performKernelLDA(dataSet.getInstances(), 1);

            logger.debug("LDA object evaluator score: {}", ldaScore);
            double score = ldaScore - ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(data.getInstances());

            storeResult(pipeline, score, (System.currentTimeMillis() - startTimestamp));
            return 1 - score;
        } catch (Exception e) {
            throw new ObjectEvaluationFailedException("Could not evaluate LDA", e);
        }
    }

}
