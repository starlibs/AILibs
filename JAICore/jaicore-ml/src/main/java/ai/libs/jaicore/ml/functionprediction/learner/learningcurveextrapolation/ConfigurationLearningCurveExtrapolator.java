package ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.lcnet.LCNetExtrapolationMethod;

/**
 * This class is a subclass of LearningCurveExtrapolator which deals
 * with the slightly different setup that is required by the LCNet
 * of pybnn
 *
 * @author noni4
 */

public class ConfigurationLearningCurveExtrapolator extends LearningCurveExtrapolator {

	public ConfigurationLearningCurveExtrapolator(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILabeledDataset<?> dataset, final double trainsplit,
			final int[] anchorpoints, final ISamplingAlgorithmFactory<ILabeledDataset<?>, ASamplingAlgorithm<ILabeledDataset<?>>> samplingAlgorithmFactory, final long seed,
			final String identifier, final double[] configurations) throws DatasetCreationException, InterruptedException {
		super(null, learner, dataset, trainsplit, anchorpoints, samplingAlgorithmFactory, seed);
		this.extrapolationMethod = new LCNetExtrapolationMethod(identifier);
		((LCNetExtrapolationMethod) this.extrapolationMethod).setConfigurations(configurations);
	}

}
