package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.Random;

import org.api4.java.ai.ml.core.IDataConfigurable;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.common.control.IRandomConfigurable;

import ai.libs.jaicore.ml.core.evaluation.evaluator.LearningCurveExtrapolationEvaluator;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;

public class LearningCurveExtrapolationEvaluatorFactory implements ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>, IRandomConfigurable,
IDataConfigurable<ILabeledDataset<? extends ILabeledInstance>> {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;
	private ILabeledDataset<? extends ILabeledInstance> dataset;
	private Random random;

	public LearningCurveExtrapolationEvaluatorFactory(final int[] anchorpoints, final ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> subsamplingAlgorithmFactory,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearnerEvaluator() throws LearnerEvaluatorConstructionFailedException {
		return new LearningCurveExtrapolationEvaluator(this.anchorpoints, this.subsamplingAlgorithmFactory, this.dataset, this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, this.random.nextLong());
	}

	@Override
	public void setData(final ILabeledDataset<? extends ILabeledInstance> data) {
		this.dataset = data;
	}

	@Override
	public ILabeledDataset<? extends ILabeledInstance> getData() {
		return this.dataset;
	}

	@Override
	public void setRandom(final Random random) {
		this.random = random;
	}
}
