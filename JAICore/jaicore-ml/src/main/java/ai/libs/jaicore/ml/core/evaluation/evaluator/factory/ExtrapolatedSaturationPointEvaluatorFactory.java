package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.evaluation.evaluator.ExtrapolatedSaturationPointEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.IClassifierEvaluator;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.timeseries.util.WekaUtil;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.core.Instances;

public class ExtrapolatedSaturationPointEvaluatorFactory implements IClassifierEvaluatorFactory {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<ILabeledInstance, ILabeledDataset<ILabeledInstance>, ? extends ASamplingAlgorithm<ILabeledInstance, ILabeledDataset<ILabeledInstance>>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public ExtrapolatedSaturationPointEvaluatorFactory(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<ILabeledInstance, ILabeledDataset<ILabeledInstance>, ? extends ASamplingAlgorithm<ILabeledInstance, ILabeledDataset<ILabeledInstance>>> subsamplingAlgorithmFactory,
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
