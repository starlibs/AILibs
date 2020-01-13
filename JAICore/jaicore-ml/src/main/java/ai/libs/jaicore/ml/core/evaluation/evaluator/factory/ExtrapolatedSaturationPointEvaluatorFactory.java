package ai.libs.jaicore.ml.core.evaluation.evaluator.factory;

import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.IDataConfigurable;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionPerformanceMetricConfigurable;
import org.api4.java.ai.ml.core.evaluation.ISupervisedLearnerEvaluator;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.common.control.IRandomConfigurable;

import ai.libs.jaicore.ml.core.evaluation.evaluator.ExtrapolatedSaturationPointEvaluator;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;

public class ExtrapolatedSaturationPointEvaluatorFactory
		implements ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<?>>, IRandomConfigurable, IDataConfigurable<ILabeledDataset<? extends ILabeledInstance>>, IPredictionPerformanceMetricConfigurable {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;
	private ILabeledDataset<? extends ILabeledInstance> dataset;
	private Random random;
	private IDeterministicPredictionPerformanceMeasure<?, ?> metric;

	public ExtrapolatedSaturationPointEvaluatorFactory(final int[] anchorpoints, final ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> subsamplingAlgorithmFactory,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public ISupervisedLearnerEvaluator<ILabeledInstance, ILabeledDataset<?>> getLearnerEvaluator() throws LearnerEvaluatorConstructionFailedException {
		try {
			List<ILabeledDataset<?>> split = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<ILabeledDataset<?>>(), .7f, this.random).split(this.dataset);
			ILabeledDataset<?> train = split.get(0);
			ILabeledDataset<?> test = split.get(1);
			return new ExtrapolatedSaturationPointEvaluator(this.anchorpoints, this.subsamplingAlgorithmFactory, train, this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, this.random.nextLong(), test, this.metric);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LearnerEvaluatorConstructionFailedException(e);
		} catch (SplitFailedException e) {
			throw new LearnerEvaluatorConstructionFailedException(e);
		}
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

	@Override
	public void setMeasure(final IDeterministicPredictionPerformanceMeasure<?, ?> measure) {
		this.metric = measure;
	}

}
