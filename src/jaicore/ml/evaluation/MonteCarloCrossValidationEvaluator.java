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

	public MonteCarloCrossValidationEvaluator(BasicMLEvaluator basicEvaluator, int repeats, Instances data, float trainingPortion) {
		super();
		this.basicEvaluator = basicEvaluator;
		this.repeats = repeats;
		if (data == null)
			throw new IllegalArgumentException("NULL data given to MCCV!");
		this.data = data;
		this.trainingPortion = trainingPortion;
	}

	public void cancel() {
		logger.info("Received cancel");
		this.canceled = true;
	}
	
	public Double evaluate(Classifier pl) throws Exception {
		
		if (pl == null)
			throw new IllegalArgumentException("Cannot compute score for null pipeline!");

		/* perform random stratified split */
		DescriptiveStatistics stats = new DescriptiveStatistics();
		logger.info("Starting evaluation of {}", pl);
		for (int i = 0; i < repeats && !canceled; i++) {
			logger.info("Evaluating {} with split #{}/{}", pl, i + 1, repeats);
			int score = (int) Math.round(basicEvaluator.getErrorRateForRandomSplit(pl, data, trainingPortion) * 100);
			logger.info("Score for evaluation of {} with split #{}/{}: {}", pl, i + 1, repeats, score);
			stats.addValue(score);
		}

		Double score = stats.getMean();
		logger.info("Obtained score of {} for classifier {}.", score, pl);
		return score;
	}

	public BasicMLEvaluator getEvaluator() {
		return basicEvaluator;
	}
}
