package jaicore.ml.evaluation;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class MonteCarloCrossValidationEvaluator implements ClassifierEvaluator {

	static final Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationEvaluator.class);
	private final BasicMLEvaluator basicEvaluator;
	private final Instances data;
	private boolean canceled = false;
	private final int repeats;
	private final float trainingPortion;
	private final DescriptiveStatistics stats = new DescriptiveStatistics();

	public MonteCarloCrossValidationEvaluator(final BasicMLEvaluator basicEvaluator, final int repeats, final Instances data, final float trainingPortion) {
		super();
		this.basicEvaluator = basicEvaluator;
		this.repeats = repeats;
		if (data == null) {
			throw new IllegalArgumentException("NULL data given to MCCV!");
		}
		this.data = data;
		this.trainingPortion = trainingPortion;
	}

	public void cancel() {
		logger.info("Received cancel");
		this.canceled = true;
	}

	@Override
	public Double evaluate(final Classifier pl) throws Exception {
		if (pl == null) {
			throw new IllegalArgumentException("Cannot compute score for null pipeline!");
		}

		/* perform random stratified split */
		logger.info("Starting evaluation of {}", pl);
		for (int i = 0; i < this.repeats && !this.canceled && !Thread.currentThread().isInterrupted(); i++) {
			logger.info("Evaluating {} with split #{}/{}", pl, i + 1, this.repeats);
			double score = this.basicEvaluator.getErrorRateForRandomSplit(pl, this.data, this.trainingPortion);
			logger.info("Score for evaluation of {} with split #{}/{}: {}", pl, i + 1, this.repeats, score);
			stats.addValue(score);
		}
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException("MCCV has been interrupted");

		Double score = stats.getMean();
		logger.info("Obtained score of {} for classifier {}.", score, pl);
		return score;
	}

	public BasicMLEvaluator getEvaluator() {
		return this.basicEvaluator;
	}

	public DescriptiveStatistics getStats() {
		return stats;
	}
}
