package ai.libs.jaicore.ml.evaluation.evaluators.weka;

import org.api4.java.ai.ml.LearningCurve;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstance;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import ai.libs.jaicore.ml.learningcurve.extrapolation.ConfigurationLearningCurveExtrapolator;
import weka.classifiers.Classifier;

/**
 * Predicts the accuracy of a classifier with certain configurations on a point
 * of its learning curve, given some anchorpoint and its configurations using
 * the LCNet of pybnn
 *
 * Note: This code was copied from LearningCurveExtrapolationEvaluator and
 * slightly reworked
 *
 * @author noni4
 */

public class ConfigurationLearningCurveExtrapolationEvaluator implements IClassifierEvaluator {

	private Logger logger = LoggerFactory.getLogger(ConfigurationLearningCurveExtrapolationEvaluator.class);

	// Configuration for the learning curve extrapolator.
	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<Double, Double, WekaInstance, WekaInstances, ASamplingAlgorithm<Double, Double, WekaInstance, WekaInstances>> samplingAlgorithmFactory;
	private WekaInstances dataset;
	private double trainSplitForAnchorpointsMeasurement;
	private long seed;
	private String identifier;
	private double[] configurations;
	private int fullDatasetSize = -1;

	public ConfigurationLearningCurveExtrapolationEvaluator(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<Double, Double, WekaInstance, WekaInstances, ASamplingAlgorithm<Double, Double, WekaInstance, WekaInstances>> samplingAlgorithmFactory, final WekaInstances dataset,
			final double trainSplitForAnchorpointsMeasurement, final long seed, final String identifier, final double[] configurations) {
		super();
		this.anchorpoints = anchorpoints;
		this.samplingAlgorithmFactory = samplingAlgorithmFactory;
		this.dataset = dataset;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.seed = seed;
		this.identifier = identifier;
		this.configurations = configurations;
	}

	public void setFullDatasetSize(final int fullDatasetSize) {
		this.fullDatasetSize = fullDatasetSize;
	}

	@Override
	public Double evaluate(final Classifier classifier) throws InterruptedException, ObjectEvaluationFailedException {
		// Create the learning curve extrapolator with the given configuration.
		try {
			ConfigurationLearningCurveExtrapolator<Double, Double, WekaInstance, WekaInstances> extrapolator = new ConfigurationLearningCurveExtrapolator<>(classifier, this.dataset, this.trainSplitForAnchorpointsMeasurement,
					this.anchorpoints, this.samplingAlgorithmFactory, this.seed, this.identifier, this.configurations);

			// Create the extrapolator and calculate the accuracy the classifier would have
			// if it was trained on the complete dataset.
			LearningCurve learningCurve = extrapolator.extrapolateLearningCurve();

			int evaluationPoint = this.dataset.size();
			// Overwrite evaluation point if a value was provided, otherwise evaluate on the
			// size of the given dataset
			if (this.fullDatasetSize != -1) {
				evaluationPoint = this.fullDatasetSize;
			}

			return learningCurve.getCurveValue(evaluationPoint) * 100.0d;
		} catch (Exception e) {
			this.logger.warn("Evaluation of classifier failed due Exception {} with message {}. Returning null.", e.getClass().getName(), e.getMessage());
			return null;
		}
	}

}
