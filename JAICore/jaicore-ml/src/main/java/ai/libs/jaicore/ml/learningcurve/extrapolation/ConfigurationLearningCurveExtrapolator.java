package ai.libs.jaicore.ml.learningcurve.extrapolation;

import org.api4.java.ai.ml.dataset.DatasetCreationException;
import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.learningcurve.extrapolation.lcnet.LCNetExtrapolationMethod;
import weka.classifiers.Classifier;

/**
 * This class is a subclass of LearningCurveExtrapolator which deals
 * with the slightly different setup that is required by the LCNet
 * of pybnn
 *
 * @author noni4
 */

public class ConfigurationLearningCurveExtrapolator<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> extends LearningCurveExtrapolator<X, Y, I, D> {

	public ConfigurationLearningCurveExtrapolator(final Classifier learner, final D dataset, final double trainsplit, final int[] anchorpoints,
			final ISamplingAlgorithmFactory<X, Y, I, D, ASamplingAlgorithm<X, Y, I, D>> samplingAlgorithmFactory, final long seed, final String identifier, final double[] configurations)
			throws DatasetCreationException, InterruptedException {
		super(null, learner, dataset, trainsplit, anchorpoints, samplingAlgorithmFactory, seed);
		this.extrapolationMethod = new LCNetExtrapolationMethod(identifier);
		((LCNetExtrapolationMethod) this.extrapolationMethod).setConfigurations(configurations);
	}

}
