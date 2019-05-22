package jaicore.ml.evaluation.evaluators.weka.factory;

import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.dataset.util.StratifiedSplit;
import jaicore.ml.core.dataset.weka.WekaInstance;
import jaicore.ml.core.dataset.weka.WekaInstances;
import jaicore.ml.evaluation.evaluators.weka.ExtrapolatedSaturationPointEvaluator;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import weka.core.Instances;

public class ExtrapolatedSaturationPointEvaluatorFactory implements IClassifierEvaluatorFactory {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<WekaInstances, ? extends ASamplingAlgorithm<WekaInstances>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public ExtrapolatedSaturationPointEvaluatorFactory(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<WekaInstances, ? extends ASamplingAlgorithm<WekaInstances>> subsamplingAlgorithmFactory,
					final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public IClassifierEvaluator getIClassifierEvaluator(final Instances dataset, final long seed) {
		StratifiedSplit<WekaInstance, WekaInstances> split = new StratifiedSplit<>(new WekaInstances(dataset), seed);
		try {
			split.doSplit(0.7);
		} catch (AlgorithmException e) {
			throw new RuntimeException("Cannot compute split", e);
		}
		WekaInstances train = split.getTrainingData();
		WekaInstances test = split.getTestData();
		return new ExtrapolatedSaturationPointEvaluator<>(this.anchorpoints, this.subsamplingAlgorithmFactory, train,
				this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, seed, test);
	}

}
