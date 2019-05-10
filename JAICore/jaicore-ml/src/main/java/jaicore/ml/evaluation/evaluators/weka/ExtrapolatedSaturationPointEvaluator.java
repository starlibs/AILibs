package jaicore.ml.evaluation.evaluators.weka;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.dataset.weka.WekaInstances;
import jaicore.ml.interfaces.AnalyticalLearningCurve;
import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolator;
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
public class ExtrapolatedSaturationPointEvaluator<I extends IInstance> implements IClassifierEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ExtrapolatedSaturationPointEvaluator.class);

	private static final double DEFAULT_EPSILON = 0.1;

	// Configuration for the learning curve extrapolator.
	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<I, ? extends ASamplingAlgorithm<I>> samplingAlgorithmFactory;
	private IDataset<I> train;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;
	private long seed;

	// Configuration for the measurement at the saturation point.
	private double epsilon;
	private IDataset<I> test;

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
	public ExtrapolatedSaturationPointEvaluator(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<I, ? extends ASamplingAlgorithm<I>> samplingAlgorithmFactory,
					final IDataset<I> train, final double trainSplitForAnchorpointsMeasurement,
					final LearningCurveExtrapolationMethod extrapolationMethod, final long seed, final IDataset<I> test) {
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
		LearningCurveExtrapolator extrapolator = new LearningCurveExtrapolator(this.extrapolationMethod, classifier,
				this.train, this.trainSplitForAnchorpointsMeasurement, this.anchorpoints, this.samplingAlgorithmFactory, this.seed);
		try {
			// Create the extrapolator and calculate sample size of the saturation point
			// with the given epsilon
			AnalyticalLearningCurve learningCurve = (AnalyticalLearningCurve) extrapolator
					.extrapolateLearningCurve();
			int optimalSampleSize = Math.min(this.train.size(), (int) learningCurve.getSaturationPoint(this.epsilon));

			// Create a subsample with this size
			ASamplingAlgorithm<I> samplingAlgorithm = this.samplingAlgorithmFactory
					.getAlgorithm(optimalSampleSize, this.train, new Random(this.seed));
			IDataset<I> saturationPointTrainSet = samplingAlgorithm.call();
			Instances saturationPointInstances = ((WekaInstances)saturationPointTrainSet).getList();

			// Measure the accuracy with this subsample
			Instances testInstances = ((WekaInstances)this.test).getList();
			FixedSplitClassifierEvaluator evaluator = new FixedSplitClassifierEvaluator(saturationPointInstances,
					testInstances);
			return evaluator.evaluate(classifier);
		} catch (AlgorithmException | InvalidAnchorPointsException | AlgorithmExecutionCanceledException e) {
			logger.warn("Evaluation of classifier failed due Exception {} with message {}. Returning null.",
					e.getClass().getName(), e.getMessage());
			return null;
		}
	}

}
