package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;

import ai.libs.jaicore.ml.core.evaluation.evaluator.ExtrapolatedSaturationPointEvaluator;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;

public class ExtrapolatedSaturationPointEvaluatorFactory implements ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<ILabeledInstance>> {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, ? extends ASamplingAlgorithm<ILabeledDataset<? extends ILabeledInstance>>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public ExtrapolatedSaturationPointEvaluatorFactory(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, ? extends ASamplingAlgorithm<ILabeledDataset<? extends ILabeledInstance>>> subsamplingAlgorithmFactory,
					final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<ILabeledInstance>> getDataspecificRandomizedLearnerEvaluator(final ILabeledDataset<ILabeledInstance> dataset, final ISupervisedLearnerMetric metric,
			final Random random) throws LearnerEvaluatorConstructionFailedException {
		try {
			List<ILabeledDataset<ILabeledInstance>> split = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<ILabeledInstance, ILabeledDataset<ILabeledInstance>>(), .7f, random).split(dataset);
			ILabeledDataset<ILabeledInstance> train = split.get(0);
			ILabeledDataset<ILabeledInstance> test = split.get(1);
			return new ExtrapolatedSaturationPointEvaluator(this.anchorpoints, this.subsamplingAlgorithmFactory, train, this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, random.nextLong(), test, metric.getMeasure());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LearnerEvaluatorConstructionFailedException(e);
		} catch (SplitFailedException e) {
			throw new LearnerEvaluatorConstructionFailedException(e);
		}
	}

}
