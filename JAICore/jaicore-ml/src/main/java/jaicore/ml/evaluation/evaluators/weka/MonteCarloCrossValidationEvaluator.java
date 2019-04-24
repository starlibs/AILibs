package jaicore.ml.evaluation.evaluators.weka;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.evaluation.evaluators.weka.measurebridge.AbstractEvaluatorMeasureBridge;
import jaicore.ml.evaluation.evaluators.weka.measurebridge.IEvaluatorMeasureBridge;
import jaicore.ml.wekautil.dataset.splitter.IDatasetSplitter;
import jaicore.ml.wekautil.dataset.splitter.MulticlassClassStratifiedSplitter;
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
	private final IDatasetSplitter datasetSplitter;
	private final int repeats;
	private final Instances data;
	private final double trainingPortion;
	private final long seed;

	/* Can either compute the loss or cache it */
	private final IEvaluatorMeasureBridge<Double> bridge;

	public MonteCarloCrossValidationEvaluator(final IEvaluatorMeasureBridge<Double> bridge, final IDatasetSplitter datasetSplitter, final int repeats, final Instances data, final double trainingPortion, final long seed) {
		super();
		this.datasetSplitter = datasetSplitter;
		this.repeats = repeats;
		this.bridge = bridge;
		this.data = data;
		this.trainingPortion = trainingPortion;
		this.seed = seed;
	}

	public MonteCarloCrossValidationEvaluator(final IEvaluatorMeasureBridge<Double> bridge, final int repeats, final Instances data, final double trainingPortion, final long seed) {
		super();
		this.datasetSplitter = new MulticlassClassStratifiedSplitter();
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
		this.logger.info("Starting MMCV evaluation of {} (Description: {})", pl.getClass().getName(), pl);
		for (int i = 0; i < this.repeats && !this.canceled; i++) {
			this.logger.debug("Obtaining predictions of {} for split #{}/{}", pl, i + 1, this.repeats);
			if (Thread.interrupted()) { // clear the interrupted field. This is Java a general convention when an InterruptedException is thrown (see Java documentation for details)
				this.logger.info("MCCV has been interrupted, leaving MCCV.");
				throw new InterruptedException("MCCV has been interrupted.");
			}
			List<Instances> split = this.datasetSplitter.split(this.data, this.seed + i, this.trainingPortion);
			try {
				double score = this.bridge.evaluateSplit(pl, split.get(0), split.get(1));
				this.logger.info("Score for evaluation of {} with split #{}/{}: {} after {}ms", pl, i + 1, this.repeats, score, (System.currentTimeMillis() - startTimestamp));
				stats.addValue(score);
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				throw new ObjectEvaluationFailedException("Could not evaluate classifier!", e);
			}
		}
		Double score = stats.getMean();
		this.logger.info("Obtained score of {} for classifier {} in {}ms.", score, pl, (System.currentTimeMillis() - startTimestamp));
		return score;
	}

	public IEvaluatorMeasureBridge<Double> getBridge() {
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
