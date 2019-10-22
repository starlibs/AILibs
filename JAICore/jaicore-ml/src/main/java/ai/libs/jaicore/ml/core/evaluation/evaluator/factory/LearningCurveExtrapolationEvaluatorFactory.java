package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.Random;

import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;

import ai.libs.jaicore.ml.core.evaluation.evaluator.LearningCurveExtrapolationEvaluator;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;

public class LearningCurveExtrapolationEvaluatorFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements ISupervisedLearnerEvaluatorFactory<I, D> {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<I, D, ? extends ASamplingAlgorithm<D>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public LearningCurveExtrapolationEvaluatorFactory(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<I, D, ? extends ASamplingAlgorithm<D>> subsamplingAlgorithmFactory,
					final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public ISupervisedLearnerEvaluator<I, D> getDataspecificRandomizedLearnerEvaluator(final D dataset, final ISupervisedLearnerMetric metric, final Random random) throws LearnerEvaluatorConstructionFailedException {
		return new LearningCurveExtrapolationEvaluator<>(this.anchorpoints, this.subsamplingAlgorithmFactory, dataset, this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, random.nextLong());
	}
}
