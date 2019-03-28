package jaicore.ml.evaluation.evaluators.weka;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * A classifier evaluator that can perform a (monte-carlo)cross-validation on
 * the given dataset. Thereby, it uses the
 * {@link AbstractEvaluatorMeasureBridge} to evaluate the classifier on a random
 * split of the dataset.
 *
 * @author fmohr, joshua
 *
 */
public class MonteCarloCrossValidationEvaluator implements IClassifierEvaluator, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationEvaluator.class);
	private boolean canceled = false;
	private final int repeats;
	private final Instances data;
	private final double trainingPortion;
	private final long seed;

	/* Can either compute the loss or cache it */
	private final AbstractEvaluatorMeasureBridge<Double, Double> bridge;

	public MonteCarloCrossValidationEvaluator(final AbstractEvaluatorMeasureBridge<Double, Double> bridge, final int repeats, final Instances data, final double trainingPortion, final long seed) {
		super();
		this.repeats = repeats;
		this.bridge = bridge;
		this.data = data;
		this.trainingPortion = trainingPortion;
		this.seed = seed;
	}

	public void cancel() {
		this.logger.info("Received cancel");
		this.canceled = true;
	}

	@Override
	public Double evaluate(final Classifier pl) throws ObjectEvaluationFailedException, InterruptedException {
		return this.evaluate(pl, new DescriptiveStatistics());
	}

	public Double evaluate(final Classifier pl, final DescriptiveStatistics stats) throws ObjectEvaluationFailedException, InterruptedException {
		if (pl == null) {
			throw new IllegalArgumentException("Cannot compute score for null pipeline!");
		}

		long startTimestamp = System.currentTimeMillis();
		/* perform random stratified split */
		this.logger.info("Starting evaluation of {}", pl);
		for (int i = 0; i < this.repeats && !this.canceled && !Thread.currentThread().isInterrupted(); i++) {
			this.logger.debug("Obtaining predictions of {} for split #{}/{}", pl, i + 1, this.repeats);
			List<Instances> split = WekaUtil.getStratifiedSplit(this.data, this.seed + i, this.trainingPortion);
			try {
				double score = this.bridge.evaluateSplit(pl, split.get(0), split.get(1));
				this.logger.info("Score for evaluation of {} with split #{}/{}: {} after {}ms", pl, i + 1, this.repeats, score, (System.currentTimeMillis() - startTimestamp));
				stats.addValue(score);
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				throw new ObjectEvaluationFailedException(e, "Could not evaluate classifier!");
			}
		}
		if (Thread.currentThread().isInterrupted()) {
			Thread.interrupted(); // clear the interrupted field. This is Java a general convention when an InterruptedException is thrown (see Java documentation for details)
			throw new InterruptedException("MCCV has been interrupted");
		}
		Double score = stats.getMean();

		this.logger.info("Obtained score of {} for classifier {} in {}ms.", score, pl, (System.currentTimeMillis() - startTimestamp));
		return score;
	}

	public AbstractEvaluatorMeasureBridge<Double, Double> getBridge() {
		return this.bridge;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger of {} from {} to {}", this, this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger of {} to {}", this, name);
	}
}
