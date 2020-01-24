package autofe.algorithm.hasco.evaluation;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import autofe.util.EvaluationUtils;

public class LDAObjectEvaluator extends AbstractHASCOFEObjectEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(LDAObjectEvaluator.class);

	@Override
	public Double evaluate(final FilterPipeline pipeline) throws InterruptedException, ObjectEvaluationFailedException {
		if (this.data == null) {
			throw new IllegalArgumentException("Data must not be null");
		}

		long startTimestamp = System.currentTimeMillis();
		logger.info("Applying and evaluating pipeline {}.", pipeline);

		DataSet dataSet = pipeline.applyFilter(this.data, false);

		logger.debug("Perform LDA");
		try {
			final double ldaScore = EvaluationUtils.performKernelLDA(dataSet.getInstances(), 1);

			logger.debug("LDA object evaluator score: {}", ldaScore);
			double score = ldaScore - ATT_COUNT_PENALTY * EvaluationUtils.calculateAttributeCountPenalty(this.data.getInstances());

			this.storeResult(pipeline, score, (System.currentTimeMillis() - startTimestamp));
			return 1 - score;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not evaluate LDA", e);
		}
	}

}
