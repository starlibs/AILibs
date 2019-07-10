package ai.libs.jaicore.ml.learningcurve.extrapolation;

import org.api4.java.ai.ml.DatasetCreationException;
import org.api4.java.ai.ml.ILabeledAttributeArrayInstance;
import org.api4.java.ai.ml.IOrderedLabeledAttributeArrayDataset;

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

public class ConfigurationLearningCurveExtrapolator<I extends ILabeledAttributeArrayInstance<?>, D extends IOrderedLabeledAttributeArrayDataset<I, ?>> extends LearningCurveExtrapolator<I, D> {

	public ConfigurationLearningCurveExtrapolator(final Classifier learner, final D dataset, final double trainsplit, final int[] anchorpoints, final ISamplingAlgorithmFactory<I, D, ASamplingAlgorithm<I, D>> samplingAlgorithmFactory,
			final long seed, final String identifier, final double[] configurations) throws DatasetCreationException {
		super(null, learner, dataset, trainsplit, anchorpoints, samplingAlgorithmFactory, seed);
		this.extrapolationMethod = new LCNetExtrapolationMethod(identifier);
		((LCNetExtrapolationMethod) this.extrapolationMethod).setConfigurations(configurations);
	}

}
