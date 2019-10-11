package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.lcnet.LCNetExtrapolationMethod;
import weka.classifiers.Classifier;

/**
 * This class is a subclass of LearningCurveExtrapolator which deals
 * with the slightly different setup that is required by the LCNet
 * of pybnn
 *
 * @author noni4
 */

public class ConfigurationLearningCurveExtrapolator<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends LearningCurveExtrapolator<I, D> {

	public ConfigurationLearningCurveExtrapolator(final Classifier learner, final D dataset, final double trainsplit, final int[] anchorpoints, final ISamplingAlgorithmFactory<I, D, ASamplingAlgorithm<I, D>> samplingAlgorithmFactory,
			final long seed, final String identifier, final double[] configurations) throws DatasetCreationException, InterruptedException {
		super(null, learner, dataset, trainsplit, anchorpoints, samplingAlgorithmFactory, seed);
		this.extrapolationMethod = new LCNetExtrapolationMethod(identifier);
		((LCNetExtrapolationMethod) this.extrapolationMethod).setConfigurations(configurations);
	}

}
