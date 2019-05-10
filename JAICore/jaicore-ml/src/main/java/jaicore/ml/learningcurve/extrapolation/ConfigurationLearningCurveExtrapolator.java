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

	public ConfigurationLearningCurveExtrapolator(final Classifier learner, final IDataset<IInstance> dataset, final double trainsplit, final int[] anchorpoints,
			final ISamplingAlgorithmFactory<IInstance, ASamplingAlgorithm<IInstance>> samplingAlgorithmFactory, final long seed, final String identifier, final double[] configurations) {
		super(null, learner, dataset, trainsplit, anchorpoints, samplingAlgorithmFactory, seed);
		this.extrapolationMethod = new LCNetExtrapolationMethod(identifier);
		((LCNetExtrapolationMethod) this.extrapolationMethod).setConfigurations(configurations);
	}

}
