package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

public class COCOObjectEvaluator extends AbstractHASCOFEObjectEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(COCOObjectEvaluator.class);

    @Override
    public Double evaluate(final FilterPipeline object) throws InterruptedException {
        if (object == null) {
            return 20000d;
        }
        long startTimestamp = System.currentTimeMillis();

        logger.info("Applying and evaluating pipeline {}", object);
        DataSet dataSet = object.applyFilter(data, false);

        logger.debug("Applied pipeline. Starting benchmarking...");
        double loss = EvaluationUtils.calculateCOCOForBatch(dataSet.getInstances());

        logger.debug("COCO object evaluation score: {}", loss);
        storeResult(object, loss, (System.currentTimeMillis() - startTimestamp));
        return loss;
    }
}
