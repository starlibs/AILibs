package jaicore.ml.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class FixedSplitValidationEvaluator implements ClassifierEvaluator {
	static final Logger logger = LoggerFactory.getLogger(FixedSplitValidationEvaluator.class);
	private boolean canceled = false;

	private final BasicMLEvaluator basicEvaluator;
	private final Instances trainingData;
	private final Instances validationData;

	public FixedSplitValidationEvaluator(final BasicMLEvaluator basicEvaluator, final Instances trainingData,
			final Instances validationData) {
		super();
		this.basicEvaluator = basicEvaluator;

		if (trainingData == null) {
			throw new IllegalArgumentException("NULL training data given to FSV!");
		}
		this.trainingData = trainingData;

		if (validationData == null) {
			throw new IllegalArgumentException("NULL validation data given to FSV!");
		}
		this.validationData = validationData;
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
		double score = this.basicEvaluator.getErrorRateForSplit(pl, this.trainingData, this.validationData);
		logger.info("Obtained score of {} for classifier {}.", score, pl);
		return score;
	}

	public BasicMLEvaluator getEvaluator() {
		return this.basicEvaluator;
	}
}
