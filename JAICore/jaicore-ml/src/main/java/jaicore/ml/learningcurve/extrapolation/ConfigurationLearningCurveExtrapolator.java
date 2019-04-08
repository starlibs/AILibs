package jaicore.ml.learningcurve.extrapolation;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.learningcurve.extrapolation.lcnet.LCNetExtrapolationMethod;
import weka.classifiers.Classifier;

/**
 * This class is a subclass of LearningCurveExtrapolator which deals
 * with the slightly different setup that is required by the LCNet 
 * of pybnn
 * 
 * @author noni4
 */

public class ConfigurationLearningCurveExtrapolator extends LearningCurveExtrapolator {

	public ConfigurationLearningCurveExtrapolator(Classifier learner, IDataset<IInstance> dataset, double trainsplit,
			ISamplingAlgorithmFactory<IInstance, ASamplingAlgorithm<IInstance>> samplingAlgorithmFactory, long seed, String identifier, double[] configurations) {
		super(null, learner, dataset, trainsplit, samplingAlgorithmFactory, seed);
		this.extrapolationMethod = new LCNetExtrapolationMethod(identifier);
		((LCNetExtrapolationMethod) this.extrapolationMethod).setConfigurations(configurations);
	}

}
