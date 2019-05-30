package jaicore.ml.evaluation.evaluators.weka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.events.IEventEmitter;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.interfaces.LearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolatedEvent;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolator;
import weka.classifiers.Classifier;

/**
 * Evaluates a classifier by predicting its learning curve with a few
 * anchorpoints. The evaluation result is the accuracy or the error rate (configurable) for the
 * complete dataset. Depending on the chosen anchorpoints this evaluation method
 * will be really fast, but can be inaccurate depending on the learning curve
 * extrapolation method, since it will only give a prediction of the accuracy
 * and does not measure it.
 *
 * @author Lukas Brandt
 */
public class LearningCurveExtrapolationEvaluator<I extends ILabeledAttributeArrayInstance<?>, D extends IOrderedLabeledAttributeArrayDataset<I, ?>> implements IClassifierEvaluator, ILoggingCustomizable, IEventEmitter {

	private Logger logger = LoggerFactory.getLogger(LearningCurveExtrapolationEvaluator.class);

	// Configuration for the learning curve extrapolator.
	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<D, ? extends ASamplingAlgorithm<D>> samplingAlgorithmFactory;
	private D dataset;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;
	private long seed;
	private int fullDatasetSize = -1;
	private static final boolean EVALUATE_ACCURACY = false; // otherwise error rate
	private final EventBus eventBus = new EventBus();

	/**
	 * Create a classifier evaluator with learning curve extrapolation.
	 *
	 * @param anchorpoints Anchorpoints for the learning
	 *            curve extrapolation.
	 * @param samplingAlgorithmFactory Subsampling factory to create a
	 *            subsampler for the samples at the given anchorpoints.
	 * @param dataset Dataset to evaluate the classifier with.
	 * @param trainSplitForAnchorpointsMeasurement Ratio to split the subsamples at
	 *            the anchorpoints into train and test.
	 * @param extrapolationMethod Method to extrapolate a learning
	 *            curve from the accuracy
	 *            measurements at the anchorpoints.
	 * @param seed Random seed.
	 */
	public LearningCurveExtrapolationEvaluator(final int[] anchorpoints, final ISamplingAlgorithmFactory<D, ? extends ASamplingAlgorithm<D>> samplingAlgorithmFactory, final D dataset,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod, final long seed) {
		super();
		this.anchorpoints = anchorpoints;
		this.samplingAlgorithmFactory = samplingAlgorithmFactory;
		this.dataset = dataset;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
		this.seed = seed;
	}

	public void setFullDatasetSize(final int fullDatasetSize) {
		this.fullDatasetSize = fullDatasetSize;
	}

	/**
	 * Computes the (estimated) measure of the classifier on the full dataset
	 */
	@Override
	public Double evaluate(final Classifier classifier) throws InterruptedException, ObjectEvaluationFailedException {

		// Create the learning curve extrapolator with the given configuration.
		this.logger.info("Receive request to evaluate classifier {}", classifier);
		try {
			LearningCurveExtrapolator<I, D> extrapolator = new LearningCurveExtrapolator<>(this.extrapolationMethod, classifier, this.dataset, this.trainSplitForAnchorpointsMeasurement, this.anchorpoints, this.samplingAlgorithmFactory, this.seed);
			extrapolator.setLoggerName(this.getLoggerName() + ".extrapolator");
			
			/* Create the extrapolator and calculate the accuracy the classifier would have if it was trained on the complete dataset. */
			this.logger.debug("Extrapolating learning curve.");
			LearningCurve learningCurve = extrapolator.extrapolateLearningCurve();
			this.logger.debug("Retrieved learning curve {}.", learningCurve);
			this.eventBus.post(new LearningCurveExtrapolatedEvent(extrapolator));

			int evaluationPoint = this.dataset.size();

			/* Overwrite evaluation point if a value was provided, otherwise evaluate on the size of the given dataset */
			if (this.fullDatasetSize != -1) {
				evaluationPoint = this.fullDatasetSize;
			}

			Double val = EVALUATE_ACCURACY ? learningCurve.getCurveValue(evaluationPoint) : 1 - learningCurve.getCurveValue(evaluationPoint);
			this.logger.info("Estimate for performance on full dataset is {}", val);
			return val;
		} catch (AlgorithmException | InvalidAnchorPointsException | DatasetCreationException e) {
			throw new ObjectEvaluationFailedException("Could not compute a score based on the learning curve.", e);
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	/**
	 * Register observers for learning curve predictions (including estimates of the time)
	 *
	 * @param listener
	 */
	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}
}
