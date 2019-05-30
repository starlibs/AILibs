package jaicore.ml.learningcurve.extrapolation;

import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
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

public class ConfigurationLearningCurveExtrapolator<I extends ILabeledAttributeArrayInstance<?>, D extends IOrderedLabeledAttributeArrayDataset<I, ?>> extends LearningCurveExtrapolator<I, D> {

	public ConfigurationLearningCurveExtrapolator(final Classifier learner, final D dataset, final double trainsplit, final int[] anchorpoints, final ISamplingAlgorithmFactory<D, ASamplingAlgorithm<D>> samplingAlgorithmFactory,
			final long seed, final String identifier, final double[] configurations) throws DatasetCreationException {
		super(null, learner, dataset, trainsplit, anchorpoints, samplingAlgorithmFactory, seed);
		this.extrapolationMethod = new LCNetExtrapolationMethod(identifier);
		((LCNetExtrapolationMethod) this.extrapolationMethod).setConfigurations(configurations);
	}

}
