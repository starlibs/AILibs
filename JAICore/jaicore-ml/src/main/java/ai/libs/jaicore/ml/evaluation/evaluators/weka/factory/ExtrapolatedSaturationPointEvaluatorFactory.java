package ai.libs.jaicore.ml.evaluation.evaluators.weka.factory;

import java.util.List;

import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstance;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.ExtrapolatedSaturationPointEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import ai.libs.jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.weka.dataset.splitter.SplitFailedException;
import weka.core.Instances;

public class ExtrapolatedSaturationPointEvaluatorFactory implements IClassifierEvaluatorFactory {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<Double, Double, WekaInstance, WekaInstances, ? extends ASamplingAlgorithm<Double, Double, WekaInstance, WekaInstances>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public ExtrapolatedSaturationPointEvaluatorFactory(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<Double, Double, WekaInstance, WekaInstances, ? extends ASamplingAlgorithm<Double, Double, WekaInstance, WekaInstances>> subsamplingAlgorithmFactory,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public IClassifierEvaluator getIClassifierEvaluator(final Instances dataset, final long seed) throws ClassifierEvaluatorConstructionFailedException {
		try {
			List<Instances> split = WekaUtil.getStratifiedSplit(dataset, seed, 0.7);
			WekaInstances train = new WekaInstances(split.get(0));
			WekaInstances test = new WekaInstances(split.get(1));
			return new ExtrapolatedSaturationPointEvaluator<>(this.anchorpoints, this.subsamplingAlgorithmFactory, train, this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, seed, test);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ClassifierEvaluatorConstructionFailedException(e);
		} catch (ClassNotFoundException | SplitFailedException e) {
			throw new ClassifierEvaluatorConstructionFailedException(e);
		}
	}

}
