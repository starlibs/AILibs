package jaicore.ml.evaluation.evaluators.weka.factory;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.LearningCurveExtrapolationEvaluator;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;

public class LearningCurveExtrapolationEvaluatorFactory implements IClassifierEvaluatorFactory {

	private int[] anchorpoints;
	private ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory;
	private double trainSplitForAnchorpointsMeasurement;
	private LearningCurveExtrapolationMethod extrapolationMethod;

	public LearningCurveExtrapolationEvaluatorFactory(int[] anchorpoints,
			ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory,
			double trainSplitForAnchorpointsMeasurement, LearningCurveExtrapolationMethod extrapolationMethod) {
		super();
		this.anchorpoints = anchorpoints;
		this.subsamplingAlgorithmFactory = subsamplingAlgorithmFactory;
		this.trainSplitForAnchorpointsMeasurement = trainSplitForAnchorpointsMeasurement;
		this.extrapolationMethod = extrapolationMethod;
	}

	@Override
	public IClassifierEvaluator getIClassifierEvaluator(IDataset<? extends IInstance> dataset, long seed) {
		return new LearningCurveExtrapolationEvaluator(anchorpoints, subsamplingAlgorithmFactory, dataset,
				trainSplitForAnchorpointsMeasurement, extrapolationMethod, seed);
	}

}
