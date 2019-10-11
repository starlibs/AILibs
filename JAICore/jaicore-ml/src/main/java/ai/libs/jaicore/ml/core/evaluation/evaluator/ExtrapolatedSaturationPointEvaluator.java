package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.learningcurve.IAnalyticalLearningCurve;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.InvalidAnchorPointsException;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolator;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * For the classifier a learning curve will be extrapolated with a given set of
 * anchorpoints. This learning curve can predict a saturation point with a
 * tolerance epsilon. When a subsample is drawn at this saturation point it is
 * the optimal trade-off between a fast training (therefore fast classifier
 * evaluation) and dataset representability (therefore evaluation result
 * expressiveness).
 *
 * @author Lukas Brandt
 */
public class ExtrapolatedSaturationPointEvaluator<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IClassifierEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ExtrapolatedSaturationPointEvaluator.class);

	private static final double DEFAULT_EPSILON = 0.1;

	// Configuration for the learning curve extrapolator.
	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<I, D, ? extends ASamplingAlgorithm<I, D>> samplingAlgorithmFactory;
	private D train;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;
	private long seed;

	// Configuration for the measurement at the saturation point.
	private double epsilon;
	private D test;

	/**
	 * Create a classifier evaluator with an accuracy measurement at the
	 * extrapolated learning curves saturation point.
	 *
	 * @param anchorpoints
	 *            Anchorpoints for the learning curve extrapolation.
	 * @param samplingAlgorithmFactory
	 *            Subsampling factory for a subsampler to create samples at the
	 *            given anchorpoints.
	 * @param train
	 *            Dataset predict the learning curve with and where the subsample
	 *            for the measurement is drawn from.
	 * @param trainSplitForAnchorpointsMeasurement
	 *            Ratio to split the subsamples at the anchorpoints into train and
	 *            test.
	 * @param extrapolationMethod
	 *            Method to extrapolate a learning curve from the accuracy
	 *            measurements at the anchorpoints.
	 * @param seed
	 *            Random seed.
	 * @param test
	 *            Test dataset to measure the accuracy.
	 */
	public ExtrapolatedSaturationPointEvaluator(final int[] anchorpoints, final ISamplingAlgorithmFactory<I, D, ? extends ASamplingAlgorithm<I, D>> samplingAlgorithmFactory, final D train, final double trainSplitForAnchorpointsMeasurement,
			final LearningCurveExtrapolationMethod extrapolationMethod, final long seed, final D test) {
		super();
		this.anchorpoints = anchorpoints;
		this.samplingAlgorithmFactory = samplingAlgorithmFactory;
		this.train = train;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
		this.seed = seed;
		this.epsilon = DEFAULT_EPSILON;
		this.test = test;
	}

	public void setEpsilon(final double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public Double evaluate(final Classifier classifier) throws InterruptedException, ObjectEvaluationFailedException {
		// Create the learning curve extrapolator with the given configuration.
		try {
			LearningCurveExtrapolator<I, D> extrapolator = new LearningCurveExtrapolator<>(this.extrapolationMethod, classifier, this.train, this.trainSplitForAnchorpointsMeasurement, this.anchorpoints, this.samplingAlgorithmFactory,
					this.seed);
			// Create the extrapolator and calculate sample size of the saturation point
			// with the given epsilon
			IAnalyticalLearningCurve learningCurve = (IAnalyticalLearningCurve) extrapolator.extrapolateLearningCurve();
			int optimalSampleSize = Math.min(this.train.size(), (int) learningCurve.getSaturationPoint(this.epsilon));

			// Create a subsample with this size
			ASamplingAlgorithm<I, D> samplingAlgorithm = this.samplingAlgorithmFactory.getAlgorithm(optimalSampleSize, this.train, new Random(this.seed));
			D saturationPointTrainSet = samplingAlgorithm.call();
			Instances saturationPointInstances = ((WekaInstances) saturationPointTrainSet).getList();

			// Measure the accuracy with this subsample
			Instances testInstances = ((WekaInstances) this.test).getList();
			FixedSplitClassifierEvaluator evaluator = new FixedSplitClassifierEvaluator(saturationPointInstances, testInstances);
			return evaluator.evaluate(classifier);
		} catch (AlgorithmException | InvalidAnchorPointsException | AlgorithmExecutionCanceledException | DatasetCreationException | AlgorithmTimeoutedException e) {
			logger.warn("Evaluation of classifier failed due Exception {} with message {}. Returning null.", e.getClass().getName(), e.getMessage());
			return null;
		}
	}

}
