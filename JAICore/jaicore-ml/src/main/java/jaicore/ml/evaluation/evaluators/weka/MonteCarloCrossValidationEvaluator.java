package jaicore.ml.evaluation.evaluators.weka;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.events.IEvent;
import jaicore.basic.events.IEventEmitter;
import jaicore.ml.evaluation.evaluators.weka.events.MCCVSplitEvaluationEvent;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.AbstractSplitBasedClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import jaicore.ml.weka.dataset.splitter.MulticlassClassStratifiedSplitter;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * A classifier evaluator that can perform a (monte-carlo)cross-validation on
 * the given dataset. Thereby, it uses the
 * {@link AbstractSplitBasedClassifierEvaluator} to evaluate the classifier on a random
 * split of the dataset.
 *
 * @author fmohr, joshua
 *
 */
public class MonteCarloCrossValidationEvaluator implements IClassifierEvaluator, ILoggingCustomizable, IEventEmitter {

	private final EventBus eventBus = new EventBus();
	private boolean hasListeners;
	private Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationEvaluator.class);
	private boolean canceled = false;
	private final IDatasetSplitter datasetSplitter;
	private final int repeats;
	private final Instances data;
	private final double trainingPortion;
	private final long seed;

	/* Can either compute the loss or cache it */
	private final ISplitBasedClassifierEvaluator<Double> splitBasedEvaluator;

	public MonteCarloCrossValidationEvaluator(final ISplitBasedClassifierEvaluator<Double> splitBasedEvaluator, final IDatasetSplitter datasetSplitter, final int repeats, final Instances data, final double trainingPortion,
			final long seed) {
		super();
		if (data == null) {
			throw new IllegalArgumentException("Cannot work with NULL data");
		}
		if (splitBasedEvaluator == null) {
			throw new IllegalArgumentException("Cannot work with NULL split based evaluator");
		}
		this.datasetSplitter = datasetSplitter;
		this.repeats = repeats;
		this.splitBasedEvaluator = splitBasedEvaluator;
		if (this.splitBasedEvaluator instanceof IEventEmitter) {
			((IEventEmitter)splitBasedEvaluator).registerListener(this);
		}
		this.data = data;
		this.trainingPortion = trainingPortion;
		this.seed = seed;
	}

	public MonteCarloCrossValidationEvaluator(final ISplitBasedClassifierEvaluator<Double> splitBasedEvaluator, final int repeats, final Instances data, final double trainingPortion, final long seed) {
		this(splitBasedEvaluator, new MulticlassClassStratifiedSplitter(), repeats, data, trainingPortion, seed);
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
				long startTimeForSplitEvaluation = System.currentTimeMillis();
				double score = this.splitBasedEvaluator.evaluateSplit(pl, split.get(0), split.get(1));
				if (this.hasListeners) {
					this.eventBus.post(new MCCVSplitEvaluationEvent(pl, split.get(0).size(), split.get(1).size(), (int)(System.currentTimeMillis() - startTimeForSplitEvaluation), score));
				}
				this.logger.info("Score for evaluation of {} with split #{}/{}: {} after {}ms", pl.getClass().getName(), i + 1, this.repeats, score, (System.currentTimeMillis() - startTimestamp));
				stats.addValue(score);
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				throw new ObjectEvaluationFailedException("Could not evaluate classifier!", e);
			}
		}
		Double score = stats.getMean();
		this.logger.info("Obtained score of {} for classifier {} in {}ms.", score, pl.getClass().getName(), (System.currentTimeMillis() - startTimestamp));
		return score;
	}

	public ISplitBasedClassifierEvaluator<Double> getBridge() {
		return this.splitBasedEvaluator;
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

	@Override
	public void registerListener(final Object listener) {
		this.hasListeners = true;
		this.eventBus.register(listener);
	}

	/* forward all events potentially coming in from the split evaluator */
	@Subscribe
	public void receiveEvent(final IEvent event) {
		this.eventBus.post(event);
	}
}
