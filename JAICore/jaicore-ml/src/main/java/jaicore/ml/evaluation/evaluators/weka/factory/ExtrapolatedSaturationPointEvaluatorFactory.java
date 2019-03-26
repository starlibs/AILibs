package jaicore.ml.evaluation.evaluators.weka.factory;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.dataset.util.StratifiedSplit;
import jaicore.ml.evaluation.evaluators.weka.ExtrapolatedSaturationPointEvaluator;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;

public class ExtrapolatedSaturationPointEvaluatorFactory implements IClassifierEvaluatorFactory {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public ExtrapolatedSaturationPointEvaluatorFactory(int[] anchorpoints,
			ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory,
			double trainSplitForAnchorpointsMeasurement, LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public IClassifierEvaluator getIClassifierEvaluator(IDataset<IInstance> dataset, long seed) {
		StratifiedSplit split = new StratifiedSplit(dataset, seed);
		split.doSplit(0.7);
		IDataset<IInstance> train = split.getTrainingData();
		IDataset<IInstance> test = split.getTestData();
		return new ExtrapolatedSaturationPointEvaluator(anchorpoints, subsamplingAlgorithmFactory, train,
				trainSplitForAnchorpointsMeasurement, extrapolationMethod, seed, 0.1, test);
	}

}
