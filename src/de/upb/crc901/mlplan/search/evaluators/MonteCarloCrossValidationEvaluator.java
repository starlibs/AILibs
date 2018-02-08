package de.upb.crc901.mlplan.search.evaluators;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.SolutionEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

@SuppressWarnings("serial")
public class MonteCarloCrossValidationEvaluator implements SolutionEvaluator {

	static final Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationEvaluator.class);
	private final BasicMLEvaluator basicEvaluator;
	private Instances data;
	private boolean canceled = false;
	private final int repeats;
	private final float trainingPortion;

	public MonteCarloCrossValidationEvaluator(BasicMLEvaluator basicEvaluator, int repeats, float trainingPortion) {
		super();
		this.basicEvaluator = basicEvaluator;
		this.repeats = repeats;
		this.trainingPortion = trainingPortion;
	}

	@Override
	public void cancel() {
		logger.info("Received cancel");
		this.canceled = true;
	}

	@Override
	public void setData(Instances data) {
		if (this.data != null)
			throw new IllegalStateException("Cannot reset the data");
		this.data = data;
	}

	@Override
	public Integer getSolutionScore(Classifier pl) throws Throwable {

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

		int score = (int) Math.round(stats.getMean());
		logger.info("Obtained score of {} for classifier {}.", score, pl);
		return score;
	}

	public BasicMLEvaluator getEvaluator() {
		return basicEvaluator;
	}
}
