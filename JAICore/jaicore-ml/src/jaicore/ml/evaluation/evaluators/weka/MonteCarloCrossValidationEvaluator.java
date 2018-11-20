package jaicore.ml.evaluation.evaluators.weka;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * A classifier evaluator that can perform a (monte-carlo)cross-validation on
 * the given dataset. Thereby, it uses the
 * {@link AbstractEvaluatorMeasureBridge} to evaluate the classifier on a random
 * split of the dataset.
 * 
 * @author joshua
 *
 */
public class MonteCarloCrossValidationEvaluator implements IClassifierEvaluator {

	static final Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationEvaluator.class);
	private boolean canceled = false;
	private final int repeats;
	private final Instances data;
	private final float trainingPortion;
	private final Random rand;
	/* Can either compute the loss or cache it */
	private final AbstractEvaluatorMeasureBridge<Double, Double> bridge;

	private final DescriptiveStatistics stats = new DescriptiveStatistics();

	public MonteCarloCrossValidationEvaluator(AbstractEvaluatorMeasureBridge<Double, Double> bridge,
			final int repeats, final Instances data, final float trainingPortion, final int seed) {
		super();
		this.repeats = repeats;
		this.bridge = bridge;
		this.data = data;
		this.trainingPortion = trainingPortion;
		this.rand = new Random(seed);
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
			logger.debug("Obtaining predictions of {} for split #{}/{}", pl, i + 1, this.repeats);
			List<Instances> split = WekaUtil.getStratifiedSplit(data, rand, trainingPortion);
			double score = bridge.evaluateSplit(pl, split.get(0), split.get(1));
			logger.info("Score for evaluation of {} with split #{}/{}: {}", pl, i + 1, this.repeats, score);
		}
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException("MCCV has been interrupted");

		Double score = stats.getMean();
		logger.info("Obtained score of {} for classifier {}.", score, pl);
		return score;
	}

	public DescriptiveStatistics getStats() {
		return stats;
	}

	public AbstractEvaluatorMeasureBridge<Double, Double> getBridge() {
		return bridge;
	}
}
