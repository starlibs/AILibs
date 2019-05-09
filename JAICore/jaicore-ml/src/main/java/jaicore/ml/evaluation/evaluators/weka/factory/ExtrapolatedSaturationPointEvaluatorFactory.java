package jaicore.ml.evaluation.evaluators.weka.factory;

import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.dataset.util.StratifiedSplit;
import jaicore.ml.core.dataset.weka.WekaInstancesUtil;
import jaicore.ml.evaluation.evaluators.weka.ExtrapolatedSaturationPointEvaluator;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import weka.core.Instances;

public class ExtrapolatedSaturationPointEvaluatorFactory implements IClassifierEvaluatorFactory {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public ExtrapolatedSaturationPointEvaluatorFactory(final int[] anchorpoints,
			final ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory,
					final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public IClassifierEvaluator getIClassifierEvaluator(final Instances dataset, final long seed) {
		StratifiedSplit split = new StratifiedSplit(WekaInstancesUtil.wekaInstancesToDataset(dataset), seed);
		try {
			split.doSplit(0.7);
		} catch (AlgorithmException e) {
			throw new RuntimeException("Cannot compute split", e);
		}
		IDataset<IInstance> train = split.getTrainingData();
		IDataset<IInstance> test = split.getTestData();
		return new ExtrapolatedSaturationPointEvaluator(this.anchorpoints, this.subsamplingAlgorithmFactory, train,
				this.trainSplitForAnchorpointsMeasurement, this.extrapolationMethod, seed, test);
	}

}
