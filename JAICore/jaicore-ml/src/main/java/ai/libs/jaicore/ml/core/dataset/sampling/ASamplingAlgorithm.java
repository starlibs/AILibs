package ai.libs.jaicore.ml.core.dataset.sampling;

import org.api4.java.ai.ml.ISamplingAlgorithm;
import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;

/**
 * An abstract class for sampling algorithms providing basic functionality of an algorithm.
 *
 * @author wever
 */
public abstract class ASamplingAlgorithm<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> extends AAlgorithm<D, D> implements ISamplingAlgorithm<X, Y, I, D> {

	protected ASamplingAlgorithm(final IOwnerBasedAlgorithmConfig config, final D input) {
		super(config, input);
	}

	protected ASamplingAlgorithm(final D input) {
		super(input);
	}
}
