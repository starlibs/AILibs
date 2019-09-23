package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.evaluation.evaluator.IClassifierEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.LearningCurveExtrapolationEvaluator;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.core.tabular.funcpred.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.core.Instances;

public class LearningCurveExtrapolationEvaluatorFactory implements IClassifierEvaluatorFactory {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<ILabeledInstance, ILabeledDataset<ILabeledInstance>, ? extends ASamplingAlgorithm<ILabeledInstance, ILabeledDataset<ILabeledInstance>>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public LearningCurveExtrapolationEvaluatorFactory(final int[] anchorpoints,
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
			return new LearningCurveExtrapolationEvaluator<>(this.anchorpoints, this.subsamplingAlgorithmFactory, new WekaInstances(dataset), this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, seed);
		} catch (ClassNotFoundException e) {
			throw new ClassifierEvaluatorConstructionFailedException(e);
		}
	}
}
